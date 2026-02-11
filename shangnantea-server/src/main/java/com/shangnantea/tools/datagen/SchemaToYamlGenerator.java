package com.shangnantea.tools.datagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单的数据库表结构导出工具，用于生成数据生成规则的 YAML 骨架文件。
 *
 * 使用说明（本地运行）：
 * 1. 确保 application.yml / application-local.yml 中配置了 spring.datasource
 * 2. 在 IDE 中运行 main 方法
 * 3. 生成的文件默认输出到项目根目录下：data-generator-rules-skeleton.yml
 *
 * 注意：本工具只生成“骨架”，不会推断具体生成规则，后续需要手工为各字段补充 type / 规则。
 */
public class SchemaToYamlGenerator {

    private record ColumnInfo(String name, String dataType, Integer length, boolean nullable) {}

    private record DataSourceConfig(String url, String username, String password, String schema) {}

    public static void main(String[] args) throws SQLException, IOException {
        System.out.println("[DataGen] 开始从数据库导出表结构生成 YAML 规则骨架...");

        DataSourceConfig ds = loadDataSourceConfig();
        try (Connection conn = DriverManager.getConnection(ds.url(), ds.username(), ds.password())) {
            Map<String, List<ColumnInfo>> tables = loadSchema(conn, ds.schema());
            String yaml = buildYamlSkeleton(tables);

            Path out = Path.of("data-generator-rules-skeleton.yml");
            Files.writeString(out, yaml, StandardCharsets.UTF_8);
            System.out.println("[DataGen] 规则骨架已生成: " + out.toAbsolutePath());
        }
    }

    private static Map<String, List<ColumnInfo>> loadSchema(Connection conn, String schema) throws SQLException {
        String sql = """
                SELECT TABLE_NAME,
                       COLUMN_NAME,
                       DATA_TYPE,
                       CHARACTER_MAXIMUM_LENGTH,
                       IS_NULLABLE
                  FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = ?
                 ORDER BY TABLE_NAME, ORDINAL_POSITION
                """;

        Map<String, List<ColumnInfo>> result = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String table = rs.getString("TABLE_NAME");
                    String col = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("DATA_TYPE");
                    Integer len = rs.getObject("CHARACTER_MAXIMUM_LENGTH") != null
                            ? rs.getInt("CHARACTER_MAXIMUM_LENGTH")
                            : null;
                    boolean nullable = "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE"));

                    result.computeIfAbsent(table, k -> new ArrayList<>())
                          .add(new ColumnInfo(col, dataType, len, nullable));
                }
            }
        }
        return result;
    }

    private static String buildYamlSkeleton(Map<String, List<ColumnInfo>> tables) {
        String indent1 = "  ";
        String indent2 = "    ";
        String indent3 = "      ";

        StringBuilder sb = new StringBuilder();
        sb.append("# 自动生成的测试数据规则骨架文件\n");
        sb.append("# 生成时间: ").append(LocalDateTime.now()).append('\n');
        sb.append("# 请根据实际业务为每个字段的 type / 规则进行补充，例如：\n");
        sb.append("#   type: sequence / faker / enum / password / phone / email / const / randomDate 等\n");
        sb.append("tables:\n");

        for (Map.Entry<String, List<ColumnInfo>> entry : tables.entrySet()) {
            String table = entry.getKey();
            List<ColumnInfo> cols = entry.getValue();

            sb.append(indent1).append(table).append(":\n");
            sb.append(indent2).append("defaultCount: 100  # TODO: 默认生成数量\n");
            sb.append(indent2).append("fields:\n");

            for (ColumnInfo c : cols) {
                sb.append(indent3).append(c.name()).append(":\n");
                sb.append(indent3).append("  type: TODO  # TODO: 根据业务选择生成类型\n");
                sb.append(indent3).append("  sqlType: ").append(c.dataType());
                if (c.length() != null) {
                    sb.append('(').append(c.length()).append(')');
                }
                sb.append('\n');
                sb.append(indent3).append("  nullable: ").append(c.nullable()).append('\n');
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * 从 application.yml 和 application-local.yml 中加载 spring.datasource 配置。
     * - 以 application.yml 为基础
     * - application-local.yml 中的 spring.datasource.password 会覆盖默认密码
     */
    @SuppressWarnings("unchecked")
    private static DataSourceConfig loadDataSourceConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // 读取 application.yml
        InputStream appIs = SchemaToYamlGenerator.class.getResourceAsStream("/application.yml");
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

        // 覆盖密码：优先从 application-local.yml 中读取 spring.datasource.password
        InputStream localIs = SchemaToYamlGenerator.class.getResourceAsStream("/application-local.yml");
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

        String schema = extractSchemaFromUrl(url);
        System.out.println("[DataGen] 使用数据源配置: url=" + url + ", username=" + username + ", schema=" + schema);
        return new DataSourceConfig(url, username, password, schema);
    }

    /**
     * 从 JDBC URL 中解析数据库名，例如：
     * jdbc:mysql://localhost:3306/teasystem.sql?xxx -> teasystem.sql
     */
    private static String extractSchemaFromUrl(String url) {
        int slash = url.lastIndexOf('/');
        if (slash < 0 || slash + 1 >= url.length()) {
            throw new IllegalArgumentException("无法从 URL 解析数据库名: " + url);
        }
        int q = url.indexOf('?', slash);
        if (q < 0) q = url.length();
        return url.substring(slash + 1, q);
    }
}

