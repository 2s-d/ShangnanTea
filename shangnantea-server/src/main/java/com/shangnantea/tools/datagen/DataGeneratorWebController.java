package com.shangnantea.tools.datagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.shangnantea.security.util.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内部数据生成 Web 控制台。
 *
 * 访问地址（开发环境）：http://localhost:8080/api/internal/datagen
 */
@Controller
@RequestMapping("/internal/datagen")
public class DataGeneratorWebController {

    private static final Path RULES_PATH = Path.of("data-generator-rules.yml");

    private record DataSourceConfig(String url, String username, String password) {}

    private DataGenConfig loadConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(Files.newInputStream(RULES_PATH), DataGenConfig.class);
    }

    @GetMapping
    public String index(Model model) throws IOException {
        if (!Files.exists(RULES_PATH)) {
            model.addAttribute("error", "未找到规则文件: " + RULES_PATH.toAbsolutePath());
            return "datagen";
        }
        DataGenConfig config = loadConfig();
        Map<String, TableRule> allTables = config.getTables();
        
        // 找出所有关联表（被其他表的relatedTables引用的表）
        java.util.Set<String> relatedTableNames = new java.util.HashSet<>();
        for (TableRule tableRule : allTables.values()) {
            if (tableRule.getRelatedTables() != null) {
                relatedTableNames.addAll(tableRule.getRelatedTables().keySet());
            }
        }
        
        // 构建显示用的表信息（包含是否关联表的标记和关联信息）
        Map<String, TableDisplayInfo> displayTables = new LinkedHashMap<>();
        for (Map.Entry<String, TableRule> entry : allTables.entrySet()) {
            String tableName = entry.getKey();
            TableRule tableRule = entry.getValue();
            
            TableDisplayInfo info = new TableDisplayInfo();
            info.tableRule = tableRule;
            info.isRelatedTable = relatedTableNames.contains(tableName);
            
            // 构建关联信息说明
            if (info.isRelatedTable) {
                StringBuilder relatedInfo = new StringBuilder("关联表 - ");
                // 找出哪些主表引用了这个关联表
                java.util.List<String> mainTables = new java.util.ArrayList<>();
                for (Map.Entry<String, TableRule> mainEntry : allTables.entrySet()) {
                    if (mainEntry.getValue().getRelatedTables() != null &&
                        mainEntry.getValue().getRelatedTables().containsKey(tableName)) {
                        RelatedTableRule relatedRule = mainEntry.getValue().getRelatedTables().get(tableName);
                        mainTables.add(mainEntry.getKey() + "×" + relatedRule.getCount());
                    }
                }
                if (!mainTables.isEmpty()) {
                    relatedInfo.append(String.join(", ", mainTables));
                }
                info.relatedInfo = relatedInfo.toString();
            }
            
            displayTables.put(tableName, info);
        }
        
        model.addAttribute("tables", displayTables);
        model.addAttribute("message", null);
        model.addAttribute("error", null);
        return "datagen";
    }
    
    /**
     * 表显示信息
     */
    public static class TableDisplayInfo {
        public TableRule tableRule;
        public boolean isRelatedTable;
        public String relatedInfo;
        
        // Thymeleaf访问属性（使用getter方法）
        public int getDefaultCount() { 
            return tableRule != null ? tableRule.getDefaultCount() : 100; 
        }
        public boolean getIsRelatedTable() { 
            return isRelatedTable; 
        }
        public String getRelatedInfo() { 
            return relatedInfo != null ? relatedInfo : ""; 
        }
    }

    @PostMapping("/run")
    public String run(@RequestParam(value = "tables", required = false) List<String> selectedTables,
                      @RequestParam Map<String, String> allParams,
                      Model model) throws IOException {
        DataGenConfig config = loadConfig();
        Map<String, TableRule> allTables = config.getTables();
        
        // 重新构建显示用的表信息（与index方法保持一致）
        java.util.Set<String> relatedTableNames = new java.util.HashSet<>();
        for (TableRule tableRule : allTables.values()) {
            if (tableRule.getRelatedTables() != null) {
                relatedTableNames.addAll(tableRule.getRelatedTables().keySet());
            }
        }
        
        Map<String, TableDisplayInfo> displayTables = new LinkedHashMap<>();
        for (Map.Entry<String, TableRule> entry : allTables.entrySet()) {
            String tableName = entry.getKey();
            TableRule tableRule = entry.getValue();
            
            TableDisplayInfo info = new TableDisplayInfo();
            info.tableRule = tableRule;
            info.isRelatedTable = relatedTableNames.contains(tableName);
            
            if (info.isRelatedTable) {
                StringBuilder relatedInfo = new StringBuilder("关联表 - ");
                java.util.List<String> mainTables = new java.util.ArrayList<>();
                for (Map.Entry<String, TableRule> mainEntry : allTables.entrySet()) {
                    if (mainEntry.getValue().getRelatedTables() != null &&
                        mainEntry.getValue().getRelatedTables().containsKey(tableName)) {
                        RelatedTableRule relatedRule = mainEntry.getValue().getRelatedTables().get(tableName);
                        mainTables.add(mainEntry.getKey() + "×" + relatedRule.getCount());
                    }
                }
                if (!mainTables.isEmpty()) {
                    relatedInfo.append(String.join(", ", mainTables));
                }
                info.relatedInfo = relatedInfo.toString();
            }
            
            displayTables.put(tableName, info);
        }
        
        model.addAttribute("tables", displayTables);

        if (selectedTables == null || selectedTables.isEmpty()) {
            model.addAttribute("error", "请至少选择一个需要生成数据的表。");
            return "datagen";
        }

        // 过滤掉关联表（防止用户通过修改HTML提交关联表）
        // 过滤掉关联表（防止用户通过修改HTML提交关联表）
        // 复用上面已经构建的relatedTableNames变量
        
        List<String> validTables = new java.util.ArrayList<>();
        for (String table : selectedTables) {
            if (!relatedTableNames.contains(table)) {
                validTables.add(table);
            }
        }
        
        if (validTables.isEmpty()) {
            model.addAttribute("error", "请至少选择一个主表（关联表会自动生成，无需选择）。");
            return "datagen";
        }
        
        // 解析每个表的数量
        Map<String, Integer> plan = new LinkedHashMap<>();
        for (String table : validTables) {
            String key = "count_" + table;
            String raw = allParams.getOrDefault(key, "").trim();
            int def = allTables.get(table).getDefaultCount();
            if (def <= 0) def = 100;
            int count;
            if (raw.isEmpty()) {
                count = def;
            } else {
                try {
                    count = Integer.parseInt(raw);
                    if (count <= 0) {
                        model.addAttribute("error", "表 " + table + " 的数量必须大于 0");
                        return "datagen";
                    }
                } catch (NumberFormatException e) {
                    model.addAttribute("error", "表 " + table + " 的数量格式错误，请输入整数");
                    return "datagen";
                }
            }
            plan.put(table, count);
        }

        // 执行生成
        StringBuilder msg = new StringBuilder();
        try {
            DataSourceConfig ds = loadDataSourceConfig();
            try (Connection conn = DriverManager.getConnection(ds.url(), ds.username(), ds.password())) {
                conn.setAutoCommit(false);
                PasswordEncoder passwordEncoder = new PasswordEncoder();
                DataGeneratorEngine engine = new DataGeneratorEngine(config, passwordEncoder);

                for (Map.Entry<String, Integer> e : plan.entrySet()) {
                    String tableName = e.getKey();
                    int count = e.getValue();
                    msg.append("开始为表 ").append(tableName).append(" 生成 ").append(count).append(" 行数据...\n");
                    try {
                        engine.generateForTable(conn, tableName, count);
                        conn.commit();
                        msg.append("表 ").append(tableName).append(" 生成完成。\n");
                    } catch (Exception ex) {
                        conn.rollback();
                        msg.append("表 ").append(tableName).append(" 生成失败，已回滚本表事务。错误: ")
                           .append(ex.getMessage()).append("\n");
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "数据生成过程中发生异常: " + e.getMessage());
            e.printStackTrace();
            return "datagen";
        }

        model.addAttribute("message", msg.toString());
        model.addAttribute("error", null);
        return "datagen";
    }

    /**
     * 复用 application.yml / application-local.yml 中的 spring.datasource 配置。
     */
    @SuppressWarnings("unchecked")
    private static DataSourceConfig loadDataSourceConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        InputStream appIs = DataGeneratorWebController.class.getResourceAsStream("/application.yml");
        if (appIs == null) {
            throw new IllegalStateException("未找到 application.yml，无法加载数据源配置");
        }
        Map<String, Object> root = mapper.readValue(appIs, Map.class);
        Map<String, Object> spring = (Map<String, Object>) root.get("spring");
        if (spring == null) {
            throw new IllegalStateException("application.yml 中缺少 spring 配置");
        }
        Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
        if (datasource == null) {
            throw new IllegalStateException("application.yml 中缺少 spring.datasource 配置");
        }

        String url = (String) datasource.get("url");
        String username = (String) datasource.get("username");
        String password = (String) datasource.get("password");

        // 覆盖密码：优先从 application-local.yml 读取
        InputStream localIs = DataGeneratorWebController.class.getResourceAsStream("/application-local.yml");
        if (localIs != null) {
            Map<String, Object> localRoot = mapper.readValue(localIs, Map.class);
            Map<String, Object> localSpring = (Map<String, Object>) localRoot.get("spring");
            if (localSpring != null) {
                Map<String, Object> localDs = (Map<String, Object>) localSpring.get("datasource");
                if (localDs != null && localDs.get("password") != null) {
                    password = String.valueOf(localDs.get("password"));
                }
            }
        }

        if (url == null || username == null) {
            throw new IllegalStateException("spring.datasource.url 或 username 为空，请检查 application.yml");
        }

        return new DataSourceConfig(url, username, password);
    }
}
