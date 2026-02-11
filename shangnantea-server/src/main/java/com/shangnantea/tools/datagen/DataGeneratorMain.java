package com.shangnantea.tools.datagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.shangnantea.security.util.PasswordEncoder;

import java.io.IOException;
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
 * 3. 修改下面的 JDBC_URL / JDBC_USER / JDBC_PASSWORD 使其指向本地数据库
 * 4. 在 IDE 中运行本 main 方法，即可按配置为各表生成默认数量的数据
 *
 * 后续要接 TUI / Web 界面时，只需要替换 tablesToGenerate / count 的传入方式即可。
 */
public class DataGeneratorMain {

    // TODO: 根据本地数据库配置修改以下常量
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/teasystem?useSSL=false&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";

    private static final Path RULES_PATH = Path.of("data-generator-rules.yml");

    public static void main(String[] args) throws IOException, SQLException {
        if (!Files.exists(RULES_PATH)) {
            System.err.println("[DataGen] 未找到规则文件: " + RULES_PATH.toAbsolutePath());
            System.err.println("[DataGen] 请先运行 SchemaToYamlGenerator 生成骨架文件，并复制/重命名为 data-generator-rules.yml 后补充规则。");
            return;
        }

        System.out.println("[DataGen] 从规则文件加载配置: " + RULES_PATH.toAbsolutePath());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DataGenConfig config = mapper.readValue(Files.newInputStream(RULES_PATH), DataGenConfig.class);

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);

            PasswordEncoder passwordEncoder = new PasswordEncoder();
            DataGeneratorEngine engine = new DataGeneratorEngine(config, passwordEncoder);

            // 简单策略：对规则文件中所有表，按各自 defaultCount 生成数据
            for (Map.Entry<String, TableRule> entry : config.getTables().entrySet()) {
                String tableName = entry.getKey();
                TableRule rule = entry.getValue();
                int count = rule.getDefaultCount();

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
}

