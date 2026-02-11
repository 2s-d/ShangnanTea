package com.shangnantea.tools.datagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.shangnantea.security.util.PasswordEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 交互式 TUI 版数据生成入口。
 *
 * 设计目标：
 * - 列出规则文件中的所有表，支持多选
 * - 每个表可以单独指定生成条数（默认使用 defaultCount）
 * - 最终确认后一次性执行生成
 *
 * 使用方式：
 * - 在 IDE 中运行 main，按照提示选择表和数量即可。
 */
public class DataGeneratorTui {

    private static final Path RULES_PATH = Path.of("data-generator-rules.yml");

    private record DataSourceConfig(String url, String username, String password) {}

    public static void main(String[] args) throws IOException, SQLException {
        if (!Files.exists(RULES_PATH)) {
            System.err.println("[DataGen-TUI] 未找到规则文件: " + RULES_PATH.toAbsolutePath());
            System.err.println("[DataGen-TUI] 请先确保 data-generator-rules.yml 已存在并完成规则配置。");
            return;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DataGenConfig config = mapper.readValue(Files.newInputStream(RULES_PATH), DataGenConfig.class);

        if (config.getTables() == null || config.getTables().isEmpty()) {
            System.err.println("[DataGen-TUI] 规则文件中未定义任何表");
            return;
        }

        DataSourceConfig ds = loadDataSourceConfig();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // 1. 展示所有表
        System.out.println("======================================");
        System.out.println(" 商南茶数据生成工具 - 交互式模式 (TUI)");
        System.out.println(" 规则文件: " + RULES_PATH.toAbsolutePath());
        System.out.println("======================================");
        System.out.println();

        List<String> tableNames = new ArrayList<>(config.getTables().keySet());
        for (int i = 0; i < tableNames.size(); i++) {
            String name = tableNames.get(i);
            TableRule rule = config.getTables().get(name);
            int def = rule.getDefaultCount();
            System.out.printf("%2d. %-30s defaultCount=%d%n", i + 1, name, def);
        }
        System.out.println();

        // 2. 选择要生成的表
        System.out.println("请输入要生成数据的表编号：");
        System.out.println("- 多个编号用英文逗号分隔，例如：1,3,5");
        System.out.println("- 输入 all 生成所有表");
        System.out.print("> ");

        String line = in.readLine();
        if (line == null || line.trim().isEmpty()) {
            System.out.println("[DataGen-TUI] 未选择任何表，退出。");
            return;
        }
        line = line.trim();

        List<String> selectedTables = new ArrayList<>();
        if ("all".equalsIgnoreCase(line)) {
            selectedTables.addAll(tableNames);
        } else {
            String[] parts = line.split(",");
            for (String p : parts) {
                String s = p.trim();
                if (s.isEmpty()) continue;
                try {
                    int idx = Integer.parseInt(s);
                    if (idx < 1 || idx > tableNames.size()) {
                        System.out.println("  >> 编号超出范围，忽略：" + s);
                        continue;
                    }
                    String tableName = tableNames.get(idx - 1);
                    if (!selectedTables.contains(tableName)) {
                        selectedTables.add(tableName);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("  >> 非法编号，忽略：" + s);
                }
            }
        }

        if (selectedTables.isEmpty()) {
            System.out.println("[DataGen-TUI] 有效选择为空，退出。");
            return;
        }

        // 3. 为每个选中的表设置生成条数
        Map<String, Integer> tableCounts = new LinkedHashMap<>();
        System.out.println();
        System.out.println("为每个选中的表设置生成数量（回车使用 defaultCount）：");

        for (String tableName : selectedTables) {
            TableRule rule = config.getTables().get(tableName);
            int def = rule.getDefaultCount();
            if (def <= 0) {
                def = 100;
            }

            while (true) {
                System.out.printf("- 表 %-30s 默认生成 %d 行，输入数量或直接回车：%n> ", tableName, def);
                String cLine = in.readLine();
                if (cLine == null || cLine.trim().isEmpty()) {
                    tableCounts.put(tableName, def);
                    break;
                }
                cLine = cLine.trim();
                try {
                    int val = Integer.parseInt(cLine);
                    if (val <= 0) {
                        System.out.println("  >> 数量必须大于0，请重新输入。");
                        continue;
                    }
                    tableCounts.put(tableName, val);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("  >> 数量格式错误，请输入整数。");
                }
            }
        }

        // 4. 确认生成计划
        System.out.println();
        System.out.println("即将执行的数据生成计划：");
        for (Map.Entry<String, Integer> e : tableCounts.entrySet()) {
            System.out.printf("  - 表 %-30s 生成 %d 行%n", e.getKey(), e.getValue());
        }
        System.out.println();
        System.out.print("确认生成？输入 Y 确认，其他任意键取消：");
        String confirm = in.readLine();
        if (confirm == null || !"Y".equalsIgnoreCase(confirm.trim())) {
            System.out.println("[DataGen-TUI] 取消执行，退出。");
            return;
        }

        // 5. 执行生成
        try (Connection conn = DriverManager.getConnection(ds.url(), ds.username(), ds.password())) {
            conn.setAutoCommit(false);

            PasswordEncoder passwordEncoder = new PasswordEncoder();
            DataGeneratorEngine engine = new DataGeneratorEngine(config, passwordEncoder);

            for (Map.Entry<String, Integer> e : tableCounts.entrySet()) {
                String tableName = e.getKey();
                int count = e.getValue();

                System.out.println("[DataGen-TUI] 开始为表 " + tableName + " 生成 " + count + " 行测试数据...");
                try {
                    engine.generateForTable(conn, tableName, count);
                    conn.commit();
                    System.out.println("[DataGen-TUI] 表 " + tableName + " 生成完成。");
                } catch (Exception ex) {
                    conn.rollback();
                    System.err.println("[DataGen-TUI] 表 " + tableName + " 生成失败，已回滚本表事务。错误: " + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
            }
        }

        System.out.println("[DataGen-TUI] 所有选中表的数据生成流程结束。");
    }

    /**
     * 复用 application.yml / application-local.yml 中的 spring.datasource 配置。
     * 基本沿用 DataGeneratorMain 的实现，保持行为一致。
     */
    @SuppressWarnings("unchecked")
    private static DataSourceConfig loadDataSourceConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        InputStream appIs = DataGeneratorTui.class.getResourceAsStream("/application.yml");
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
        InputStream localIs = DataGeneratorTui.class.getResourceAsStream("/application-local.yml");
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

        System.out.println("[DataGen-TUI] 使用数据源配置: url=" + url + ", username=" + username + ", password=******");
        return new DataSourceConfig(url, username, password);
    }
}

