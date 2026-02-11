package com.shangnantea.tools.datagen;

import com.shangnantea.security.util.PasswordEncoder;
import net.datafaker.Faker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 数据生成内核：根据 DataGenConfig 中的规则，为指定表批量生成测试数据。
 *
 * 当前实现只支持部分常用类型：
 * - sequence: 序列号（支持 prefix / length）
 * - faker: 基本的 name.fullName / internet.username / lorem.sentence 等
 * - password: 通过 PasswordEncoder.encode(basePassword) 生成密码哈希
 * - const: 常量
 * - enum: 从 values 数组中随机选择一个
 * - phone: 简单的中国手机号
 * - email: 若配置 fromField，则使用该字段值拼接，否则随机生成
 * - now: 当前时间戳
 * - randomDate: 在 from/to 之间随机日期时间
 * - auto / skip: 不参与 INSERT，由数据库默认值/自增处理
 *
 * 规则不足的字段会抛异常，提示需要补充配置。
 */
public class DataGeneratorEngine {

    private final DataGenConfig config;
    private final Faker faker = new Faker();
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    public DataGeneratorEngine(DataGenConfig config, PasswordEncoder passwordEncoder) {
        this.config = config;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 为指定表生成指定数量的数据。
     */
    public void generateForTable(Connection conn, String tableName, int count) throws SQLException {
        TableRule tableRule = config.getTables().get(tableName);
        if (tableRule == null) {
            throw new IllegalArgumentException("未在配置中找到表规则: " + tableName);
        }

        List<String> columns = new ArrayList<>();
        for (Map.Entry<String, FieldRule> e : tableRule.getFields().entrySet()) {
            String field = e.getKey();
            FieldRule rule = e.getValue();
            String type = rule.getType();
            if ("auto".equalsIgnoreCase(type) || "skip".equalsIgnoreCase(type)) {
                // 不参与 INSERT，由数据库处理
                continue;
            }
            columns.add(field);
        }

        if (columns.isEmpty()) {
            throw new IllegalStateException("表 " + tableName + " 没有需要生成的字段（全部为 auto/skip）");
        }

        String sql = buildInsertSql(tableName, columns);
        System.out.println("[DataGen] 表 " + tableName + " 使用 SQL: " + sql);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int batchSize = 1000;
            for (int i = 0; i < count; i++) {
                int paramIndex = 1;

                for (String col : columns) {
                    FieldRule rule = tableRule.getFields().get(col);
                    Object value = generateValue(rule, i + 1, tableRule);
                    ps.setObject(paramIndex++, value);
                }

                ps.addBatch();

                if ((i + 1) % batchSize == 0) {
                    ps.executeBatch();
                    System.out.println("[DataGen] 已插入 " + (i + 1) + " 行到表 " + tableName);
                }
            }

            ps.executeBatch();
            System.out.println("[DataGen] 表 " + tableName + " 生成完成，共 " + count + " 行");
        }
    }

    private String buildInsertSql(String tableName, List<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(columns.get(i));
        }
        sb.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
        }
        sb.append(")");
        return sb.toString();
    }

    private Object generateValue(FieldRule rule, int seq, TableRule tableRule) {
        if (rule == null) {
            throw new IllegalArgumentException("字段规则为空，请检查配置");
        }

        String type = rule.getType();
        if (type == null || type.isBlank() || "TODO".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("字段缺少生成类型(type)，请在规则中补充。sqlType=" + rule.getSqlType());
        }

        type = type.toLowerCase();

        return switch (type) {
            case "sequence" -> {
                String prefix = (String) rule.getArgOrDefault("prefix", "");
                int length = Integer.parseInt(String.valueOf(rule.getArgOrDefault("length", 6)));
                yield prefix + String.format("%0" + length + "d", seq);
            }
            case "faker" -> generateFakerValue(rule);
            case "password" -> {
                String base = (String) rule.getArgOrDefault("base", "user1234");
                yield passwordEncoder.encode(base);
            }
            case "const" -> rule.getArg("value");
            case "enum" -> {
                Object valuesObj = rule.getArg("values");
                if (!(valuesObj instanceof List<?> values) || values.isEmpty()) {
                    throw new IllegalArgumentException("enum 字段必须提供非空的 values 数组");
                }
                int idx = random.nextInt(values.size());
                yield values.get(idx);
            }
            case "phone" -> "1" + faker.number().digits(10);
            case "email" -> {
                String fromField = (String) rule.getArg("fromField");
                if (fromField != null) {
                    // 简化：使用 fromField 的值拼接一个域名，实际实现中可以通过当前行缓存支持
                    String base = fromField + seq;
                    yield base + "@example.com";
                } else {
                    yield faker.internet().emailAddress();
                }
            }
            case "now" -> Timestamp.valueOf(LocalDateTime.now());
            case "randomdate" -> generateRandomTimestamp(rule);
            case "intrange" -> {
                int min = Integer.parseInt(String.valueOf(rule.getArgOrDefault("min", 0)));
                int max = Integer.parseInt(String.valueOf(rule.getArgOrDefault("max", 100)));
                if (min > max) {
                    int tmp = min;
                    min = max;
                    max = tmp;
                }
                yield min + random.nextInt(max - min + 1);
            }
            case "decimalrange" -> {
                double min = Double.parseDouble(String.valueOf(rule.getArgOrDefault("min", 0)));
                double max = Double.parseDouble(String.valueOf(rule.getArgOrDefault("max", 100)));
                int scale = Integer.parseInt(String.valueOf(rule.getArgOrDefault("scale", 2)));
                if (min > max) {
                    double tmp = min;
                    min = max;
                    max = tmp;
                }
                double val = min + (max - min) * random.nextDouble();
                double factor = Math.pow(10, scale);
                val = Math.round(val * factor) / factor;
                yield val;
            }
            case "auto", "skip" -> null; // 不应出现在绑定参数列表
            default -> throw new IllegalArgumentException("未知字段生成类型: " + type);
        };
    }

    private Object generateFakerValue(FieldRule rule) {
        String provider = (String) rule.getArgOrDefault("provider", "lorem.sentence");
        return switch (provider) {
            case "name.fullName" -> faker.name().fullName();
            case "internet.username" -> faker.internet().username();
            case "lorem.sentence" -> faker.lorem().sentence();
            case "lorem.paragraph" -> faker.lorem().paragraph();
            default -> faker.lorem().sentence();
        };
    }

    private Timestamp generateRandomTimestamp(FieldRule rule) {
        String fromStr = (String) rule.getArgOrDefault("from", "2024-01-01");
        String toStr = (String) rule.getArgOrDefault("to", "2025-01-01");

        LocalDate fromDate = LocalDate.parse(fromStr);
        LocalDate toDate = LocalDate.parse(toStr);

        long start = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        long randomTime = start + (long) (random.nextDouble() * (end - start));
        return new Timestamp(randomTime);
    }
}

