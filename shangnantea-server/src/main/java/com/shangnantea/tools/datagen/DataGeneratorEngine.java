package com.shangnantea.tools.datagen;

import com.shangnantea.security.util.PasswordEncoder;
import net.datafaker.Faker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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
    /**
     * 外键候选值缓存，key 形如 "users.id"
     */
    private final Map<String, List<Object>> fkCache = new java.util.HashMap<>();
    
    /**
     * 当前行数据缓存，用于fkField和fkConditional类型
     * key: 字段名, value: 字段值
     */
    private final Map<String, Object> currentRowData = new java.util.HashMap<>();
    
    /**
     * 关联表数据缓存，key: "table.column", value: Map<id, rowData>
     */
    private final Map<String, Map<Object, Map<String, Object>>> relatedDataCache = new java.util.HashMap<>();

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

        List<Object> insertedIds = new ArrayList<>();
        // 查找主键字段（通常是id字段）
        String idField = "id";
        boolean hasIdField = columns.contains(idField);
        
        try (PreparedStatement ps = conn.prepareStatement(sql, hasIdField ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
            int batchSize = 1000;
            for (int i = 0; i < count; i++) {
                currentRowData.clear();
                int paramIndex = 1;
                Object currentId = null;

                for (String col : columns) {
                    FieldRule rule = tableRule.getFields().get(col);
                    Object value = generateValue(conn, rule, i + 1, tableRule, tableName, columns);
                    currentRowData.put(col, value);
                    if (col.equals(idField)) {
                        currentId = value; // 保存主键值
                    }
                    ps.setObject(paramIndex++, value);
                }

                // 保存主键值（用于关联表生成）
                if (currentId != null) {
                    insertedIds.add(currentId);
                }

                ps.addBatch();

                if ((i + 1) % batchSize == 0) {
                    ps.executeBatch();
                    // 如果是自增主键，从数据库获取生成的主键
                    if (hasIdField) {
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            int keyIndex = 0;
                            while (rs.next() && keyIndex < batchSize) {
                                Object generatedId = rs.getObject(1);
                                // 更新已保存的主键值（如果是自增的）
                                int listIndex = insertedIds.size() - batchSize + keyIndex;
                                if (listIndex >= 0 && listIndex < insertedIds.size()) {
                                    insertedIds.set(listIndex, generatedId);
                                } else {
                                    insertedIds.add(generatedId);
                                }
                                keyIndex++;
                            }
                        }
                    }
                    System.out.println("[DataGen] 已插入 " + (i + 1) + " 行到表 " + tableName);
                }
            }

            ps.executeBatch();
            // 获取剩余的主键（如果是自增的）
            if (hasIdField) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    int remainingCount = count % batchSize;
                    if (remainingCount > 0) {
                        int startIndex = insertedIds.size() - remainingCount;
                        int keyIndex = 0;
                        while (rs.next() && keyIndex < remainingCount) {
                            Object generatedId = rs.getObject(1);
                            if (startIndex + keyIndex >= 0 && startIndex + keyIndex < insertedIds.size()) {
                                insertedIds.set(startIndex + keyIndex, generatedId);
                            } else {
                                insertedIds.add(generatedId);
                            }
                            keyIndex++;
                        }
                    }
                }
            }
            System.out.println("[DataGen] 表 " + tableName + " 生成完成，共 " + count + " 行");
        }

        // 生成关联表数据
        if (tableRule.getRelatedTables() != null && !tableRule.getRelatedTables().isEmpty()) {
            generateRelatedTables(conn, tableName, insertedIds, tableRule);
        }
    }
    
    /**
     * 生成关联表数据
     */
    private void generateRelatedTables(Connection conn, String mainTableName, List<Object> mainTableIds, TableRule mainTableRule) throws SQLException {
        for (Map.Entry<String, RelatedTableRule> entry : mainTableRule.getRelatedTables().entrySet()) {
            String relatedTableName = entry.getKey();
            RelatedTableRule relatedRule = entry.getValue();
            
            System.out.println("[DataGen] 开始为表 " + mainTableName + " 生成关联表 " + relatedTableName + " 的数据");
            
            // 获取主表的主键字段名（假设为id）
            String mainTableIdField = "id";
            
            // 为每个主表记录生成关联表数据
            for (Object mainTableId : mainTableIds) {
                for (int i = 0; i < relatedRule.getCount(); i++) {
                    generateRelatedTableRow(conn, relatedTableName, mainTableName, mainTableIdField, mainTableId, relatedRule);
                }
            }
            
            System.out.println("[DataGen] 关联表 " + relatedTableName + " 生成完成，共 " + (mainTableIds.size() * relatedRule.getCount()) + " 行");
        }
    }
    
    /**
     * 生成关联表的一行数据
     */
    private void generateRelatedTableRow(Connection conn, String relatedTableName, String mainTableName, 
                                         String mainTableIdField, Object mainTableId, RelatedTableRule relatedRule) throws SQLException {
        TableRule relatedTableRule = config.getTables().get(relatedTableName);
        if (relatedTableRule == null) {
            throw new IllegalArgumentException("未找到关联表规则: " + relatedTableName);
        }
        
        List<String> columns = new ArrayList<>();
        for (Map.Entry<String, FieldRule> e : relatedTableRule.getFields().entrySet()) {
            String field = e.getKey();
            FieldRule rule = e.getValue();
            String type = rule.getType();
            if ("auto".equalsIgnoreCase(type) || "skip".equalsIgnoreCase(type)) {
                continue;
            }
            columns.add(field);
        }
        
        String sql = buildInsertSql(relatedTableName, columns);
        currentRowData.clear();
        
        // 先加载主表的所有字段值到currentRowData，供fkField等类型使用
        Map<String, Object> mainTableData = loadMainTableRowData(conn, mainTableName, mainTableIdField, mainTableId);
        currentRowData.putAll(mainTableData);
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (String col : columns) {
                FieldRule rule = relatedTableRule.getFields().get(col);
                Object value;
                
                // 如果字段在映射中，使用主表的值
                if (relatedRule.getFieldMapping().containsKey(col)) {
                    String mainTableField = relatedRule.getFieldMapping().get(col);
                    // 如果是主表ID字段映射，使用主表ID
                    if (mainTableField.equals(mainTableIdField)) {
                        value = mainTableId;
                    } else {
                        // 从主表数据中获取
                        value = mainTableData.get(mainTableField);
                        if (value == null) {
                            // 如果缓存中没有，从数据库查询
                            value = getMainTableFieldValue(conn, mainTableName, mainTableIdField, mainTableId, mainTableField);
                        }
                    }
                } else {
                    value = generateValue(conn, rule, 0, relatedTableRule, relatedTableName, columns);
                }
                
                currentRowData.put(col, value);
                ps.setObject(paramIndex++, value);
            }
            ps.executeUpdate();
        }
    }
    
    /**
     * 加载主表的一行数据到Map中
     */
    private Map<String, Object> loadMainTableRowData(Connection conn, String mainTableName, String idField, Object id) throws SQLException {
        Map<String, Object> rowData = new java.util.HashMap<>();
        // 获取主表的所有字段（简化处理，只查询常用字段）
        String sql = "SELECT * FROM " + mainTableName + " WHERE " + idField + " = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                java.sql.ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    rowData.put(columnName, value);
                }
            }
        }
        return rowData;
    }
    
    /**
     * 获取主表字段值
     */
    private Object getMainTableFieldValue(Connection conn, String mainTableName, String idField, Object id, String fieldName) throws SQLException {
        String sql = "SELECT " + fieldName + " FROM " + mainTableName + " WHERE " + idField + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1);
                }
            }
        }
        return null;
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

    private Object generateValue(Connection conn, FieldRule rule, int seq, TableRule tableRule, String tableName, List<String> columns) throws SQLException {
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
            case "uuid" -> UUID.randomUUID().toString().replace("-", "");
            case "fkrandom" -> {
                String table = String.valueOf(rule.getArgOrDefault("table", "")).trim();
                String column = String.valueOf(rule.getArgOrDefault("column", "id")).trim();
                if (table.isEmpty()) {
                    throw new IllegalArgumentException("fkrandom 字段必须配置 table 参数");
                }
                String key = table + "." + column;
                List<Object> candidates = fkCache.get(key);
                if (candidates == null) {
                    try {
                        candidates = loadFkValues(conn, table, column);
                    } catch (SQLException e) {
                        throw new RuntimeException("加载外键候选值失败: " + key, e);
                    }
                    fkCache.put(key, candidates);
                }
                if (candidates.isEmpty()) {
                    throw new IllegalStateException("外键字段使用 fkrandom 时，源表无数据: " + key + "，请先为该表生成数据");
                }
                int idx = random.nextInt(candidates.size());
                yield candidates.get(idx);
            }
            case "fkfield" -> {
                // 从关联表获取字段值
                // args: table, column, whereField, whereValue
                String table = String.valueOf(rule.getArgOrDefault("table", "")).trim();
                String column = String.valueOf(rule.getArgOrDefault("column", "")).trim();
                String whereField = String.valueOf(rule.getArgOrDefault("whereField", "")).trim();
                String whereValueField = String.valueOf(rule.getArgOrDefault("whereValueField", "")).trim();
                
                if (table.isEmpty() || column.isEmpty() || whereField.isEmpty() || whereValueField.isEmpty()) {
                    throw new IllegalArgumentException("fkfield 字段必须配置 table, column, whereField, whereValueField 参数");
                }
                
                Object whereValue = currentRowData.get(whereValueField);
                if (whereValue == null) {
                    throw new IllegalStateException("fkfield 无法找到 whereValueField: " + whereValueField);
                }
                
                yield getFieldValueFromTable(conn, table, column, whereField, whereValue);
            }
            case "fkconditional" -> {
                // 条件外键选择：根据条件从关联表选择
                // args: table, column, conditionField, conditionValueField
                String table = String.valueOf(rule.getArgOrDefault("table", "")).trim();
                String column = String.valueOf(rule.getArgOrDefault("column", "id")).trim();
                String conditionField = String.valueOf(rule.getArgOrDefault("conditionField", "")).trim();
                String conditionValueField = String.valueOf(rule.getArgOrDefault("conditionValueField", "")).trim();
                
                if (table.isEmpty() || conditionField.isEmpty() || conditionValueField.isEmpty()) {
                    throw new IllegalArgumentException("fkconditional 字段必须配置 table, conditionField, conditionValueField 参数");
                }
                
                Object conditionValue = currentRowData.get(conditionValueField);
                if (conditionValue == null) {
                    throw new IllegalStateException("fkconditional 无法找到 conditionValueField: " + conditionValueField);
                }
                
                List<Object> candidates = loadFkValuesWithCondition(conn, table, column, conditionField, conditionValue);
                if (candidates.isEmpty()) {
                    throw new IllegalStateException("fkconditional 未找到匹配的记录: " + table + "." + conditionField + " = " + conditionValue);
                }
                int idx = random.nextInt(candidates.size());
                yield candidates.get(idx);
            }
            case "fkconditionalbyfield" -> {
                // 根据字段值选择不同的表（如根据item_type选择tea/post/tea_article）
                // args: fieldMap (如: {"tea": "teas", "post": "forum_posts", "tea_article": "tea_articles"}), typeField, column
                String typeField = String.valueOf(rule.getArgOrDefault("typeField", "")).trim();
                String column = String.valueOf(rule.getArgOrDefault("column", "id")).trim();
                Object fieldMapObj = rule.getArg("fieldMap");
                
                if (typeField.isEmpty() || fieldMapObj == null) {
                    throw new IllegalArgumentException("fkconditionalbyfield 字段必须配置 typeField 和 fieldMap 参数");
                }
                
                Object typeValue = currentRowData.get(typeField);
                if (typeValue == null) {
                    throw new IllegalStateException("fkconditionalbyfield 无法找到 typeField: " + typeField);
                }
                
                @SuppressWarnings("unchecked")
                Map<String, String> fieldMap = (Map<String, String>) fieldMapObj;
                String targetTable = fieldMap.get(String.valueOf(typeValue));
                
                if (targetTable == null || targetTable.isEmpty()) {
                    throw new IllegalStateException("fkconditionalbyfield 未找到类型 " + typeValue + " 对应的表映射");
                }
                
                List<Object> candidates = loadFkValues(conn, targetTable, column);
                if (candidates.isEmpty()) {
                    throw new IllegalStateException("fkconditionalbyfield 源表无数据: " + targetTable + "." + column);
                }
                int idx = random.nextInt(candidates.size());
                yield candidates.get(idx);
            }
            case "calc" -> {
                // 计算字段
                // args: expression (如 "price * quantity")
                String expression = String.valueOf(rule.getArgOrDefault("expression", "")).trim();
                if (expression.isEmpty()) {
                    throw new IllegalArgumentException("calc 字段必须配置 expression 参数");
                }
                yield calculateExpression(expression);
            }
            case "auto", "skip" -> null; // 不应出现在绑定参数列表
            default -> throw new IllegalArgumentException("未知字段生成类型: " + type);
        };
    }

    /**
     * 从指定表/列加载外键候选值，去重后返回
     */
    private List<Object> loadFkValues(Connection conn, String table, String column) throws SQLException {
        String sql = "SELECT DISTINCT " + column + " FROM " + table;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<Object> list = new ArrayList<>();
            while (rs.next()) {
                Object val = rs.getObject(1);
                if (val != null) {
                    list.add(val);
                }
            }
            return list;
        }
    }
    
    /**
     * 根据条件从指定表/列加载外键候选值
     */
    private List<Object> loadFkValuesWithCondition(Connection conn, String table, String column, 
                                                   String conditionField, Object conditionValue) throws SQLException {
        String sql = "SELECT DISTINCT " + column + " FROM " + table + " WHERE " + conditionField + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, conditionValue);
            try (ResultSet rs = ps.executeQuery()) {
                List<Object> list = new ArrayList<>();
                while (rs.next()) {
                    Object val = rs.getObject(1);
                    if (val != null) {
                        list.add(val);
                    }
                }
                return list;
            }
        }
    }
    
    /**
     * 从关联表获取字段值
     */
    private Object getFieldValueFromTable(Connection conn, String table, String column, 
                                          String whereField, Object whereValue) throws SQLException {
        String sql = "SELECT " + column + " FROM " + table + " WHERE " + whereField + " = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, whereValue);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1);
                }
            }
        }
        return null;
    }
    
    /**
     * 计算表达式
     */
    private Object calculateExpression(String expression) {
        // 简单的表达式计算，支持 +, -, *, /
        // 例如: "price * quantity"
        try {
            // 替换当前行数据中的字段值
            String expr = expression;
            for (Map.Entry<String, Object> entry : currentRowData.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Number) {
                    expr = expr.replace(fieldName, value.toString());
                }
            }
            
            // 简单的表达式求值（仅支持基本运算）
            // 注意：这里使用简单的字符串替换，实际应该使用更安全的表达式引擎
            if (expr.contains("*")) {
                String[] parts = expr.split("\\*");
                double result = 1.0;
                for (String part : parts) {
                    result *= Double.parseDouble(part.trim());
                }
                return Math.round(result * 100.0) / 100.0; // 保留2位小数
            } else if (expr.contains("+")) {
                String[] parts = expr.split("\\+");
                double result = 0.0;
                for (String part : parts) {
                    result += Double.parseDouble(part.trim());
                }
                return Math.round(result * 100.0) / 100.0;
            } else if (expr.contains("-")) {
                String[] parts = expr.split("-");
                double result = Double.parseDouble(parts[0].trim());
                for (int i = 1; i < parts.length; i++) {
                    result -= Double.parseDouble(parts[i].trim());
                }
                return Math.round(result * 100.0) / 100.0;
            } else if (expr.contains("/")) {
                String[] parts = expr.split("/");
                double result = Double.parseDouble(parts[0].trim());
                for (int i = 1; i < parts.length; i++) {
                    result /= Double.parseDouble(parts[i].trim());
                }
                return Math.round(result * 100.0) / 100.0;
            }
            
            return Double.parseDouble(expr.trim());
        } catch (Exception e) {
            throw new RuntimeException("计算表达式失败: " + expression, e);
        }
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