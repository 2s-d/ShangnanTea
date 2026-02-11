package com.shangnantea.tools.datagen;

import java.io.IOException;
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
 * 1. 根据实际情况修改 JDBC_URL / JDBC_USER / JDBC_PASSWORD / DB_SCHEMA
 * 2. 在 IDE 中运行 main 方法，或使用 `mvn -q -DskipTests exec:java` 运行该类
 * 3. 生成的文件默认输出到项目根目录下：data-generator-rules-skeleton.yml
 *
 * 注意：本工具只生成“骨架”，不会推断具体生成规则，后续需要手工为各字段补充 type / 规则。
 */
public class SchemaToYamlGenerator {

    // TODO: 根据本地数据库配置修改以下常量
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/teasystem?useSSL=false&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";
    private static final String DB_SCHEMA = "teasystem"; // 数据库名

    private record ColumnInfo(String name, String dataType, Integer length, boolean nullable) {}

    public static void main(String[] args) throws SQLException, IOException {
        System.out.println("[DataGen] 开始从数据库导出表结构生成 YAML 规则骨架...");
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            Map<String, List<ColumnInfo>> tables = loadSchema(conn);
            String yaml = buildYamlSkeleton(tables);

            Path out = Path.of("data-generator-rules-skeleton.yml");
            Files.writeString(out, yaml, StandardCharsets.UTF_8);
            System.out.println("[DataGen] 规则骨架已生成: " + out.toAbsolutePath());
        }
    }

    private static Map<String, List<ColumnInfo>> loadSchema(Connection conn) throws SQLException {
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
            ps.setString(1, DB_SCHEMA);
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
}

