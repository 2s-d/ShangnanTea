package com.shangnantea.tools.datagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.shangnantea.security.util.PasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * 最小可运行的数据生成入口。
 *
 * 使用步骤：
 * 1. 先运行 {@link SchemaToYamlGenerator} 生成 data-generator-rules-skeleton.yml
 * 2. 复制或重命名为 data-generator-rules.yml，并为每个字段补充 type / args 规则
 * 3. 在 IDE 中运行本 main 方法，即可按配置为各表生成默认数量的数据
 *
 * JDBC 配置自动从 application.yml / application-local.yml 的 spring.datasource 中读取，
 * 无需手动修改常量。
 */
public class DataGeneratorMain {

    private static final Path RULES_PATH = Path.of("data-generator-rules.yml");

    private record DataSourceConfig(String url, String username, String password) {}

    public static void main(String[] args) throws IOException, SQLException {
        if (!Files.exists(RULES_PATH)) {
            System.err.println("[DataGen] 未找到规则文件: " + RULES_PATH.toAbsolutePath());
            System.err.println("[DataGen] 请先运行 SchemaToYamlGenerator 生成骨架文件，并复制/重命名为 data-generator-rules.yml 后补充规则。");
            return;
        }

        System.out.println("[DataGen] 从规则文件加载配置: " + RULES_PATH.toAbsolutePath());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DataGenConfig config = mapper.readValue(Files.newInputStream(RULES_PATH), DataGenConfig.class);

        DataSourceConfig ds = loadDataSourceConfig();

        // 支持通过 JVM 参数 -Ddatagen.tables=users,teas 或环境变量 DATAGEN_TABLES 只生成指定表的数据
        String tablesProperty = System.getProperty("datagen.tables");
        if (tablesProperty == null || tablesProperty.isBlank()) {
            tablesProperty = System.getenv("DATAGEN_TABLES");
        }
        java.util.Set<String> tableFilter = null;
        if (tablesProperty != null && !tablesProperty.isBlank()) {
            tableFilter = new java.util.HashSet<>();
            for (String name : tablesProperty.split(",")) {
                if (name != null && !name.isBlank()) {
                    tableFilter.add(name.trim());
                }
            }
            System.out.println("[DataGen] 仅为以下表生成数据(通过 datagen.tables 指定): " + tableFilter);
        }

        try (Connection conn = DriverManager.getConnection(ds.url(), ds.username(), ds.password())) {
            conn.setAutoCommit(false);

            PasswordEncoder passwordEncoder = new PasswordEncoder();
            DataGeneratorEngine engine = new DataGeneratorEngine(config, passwordEncoder);

            // 简单策略：对规则文件中所有表，按各自 defaultCount 生成数据
            for (Map.Entry<String, TableRule> entry : config.getTables().entrySet()) {
                String tableName = entry.getKey();
                TableRule rule = entry.getValue();
                int count = rule.getDefaultCount();

                if (tableFilter != null && !tableFilter.contains(tableName)) {
                    System.out.println("[DataGen] 表 " + tableName + " 未在 datagen.tables 过滤列表中，跳过");
                    continue;
                }

                if (count <= 0) {
                    System.out.println("[DataGen] 表 " + tableName + " 的 defaultCount <= 0，跳过");
                    continue;
                }

                System.out.println("[DataGen] 开始为表 " + tableName + " 生成 " + count + " 行测试数据...");
                engine.generateForTable(conn, tableName, count);
                conn.commit();
            }
        }

        System.out.println("[DataGen] 所有表数据生成完成。");
    }

    /**
     * 复用 application.yml / application-local.yml 中的 spring.datasource 配置。
     */
    @SuppressWarnings("unchecked")
    private static DataSourceConfig loadDataSourceConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        InputStream appIs = DataGeneratorMain.class.getResourceAsStream("/application.yml");
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
        InputStream localIs = DataGeneratorMain.class.getResourceAsStream("/application-local.yml");
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

        System.out.println("[DataGen] 使用数据源配置: url=" + url + ", username=" + username + ", password=******");
        return new DataSourceConfig(url, username, password);
    }
}

