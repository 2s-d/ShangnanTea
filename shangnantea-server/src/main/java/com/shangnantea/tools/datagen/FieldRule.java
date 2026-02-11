package com.shangnantea.tools.datagen;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单个字段的生成规则。
 *
 * 示例 YAML：
 *   password:
 *     type: password
 *     base: "user1234"
 *     sqlType: varchar(100)
 *     nullable: false
 */
public class FieldRule {

    /**
     * 生成类型：sequence / faker / enum / password / const / phone / email / now / randomDate / auto / skip / ...
     */
    private String type;

    /**
     * 其他可选参数，如：
     * - prefix / length（sequence）
     * - provider（faker）
     * - values / weights（enum）
     * - value（const）
     * - base（password）
     * - from / to（randomDate）
     * - fromField（email 等）
     */
    private Map<String, Object> args = new LinkedHashMap<>();

    /**
     * 仅用于参考的原始 SQL 类型信息（由 SchemaToYamlGenerator 填充）
     */
    private String sqlType;

    /**
     * 是否可为空（由 SchemaToYamlGenerator 填充）
     */
    private boolean nullable;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    // 便捷方法
    public Object getArg(String key) {
        return args.get(key);
    }

    public Object getArgOrDefault(String key, Object defaultValue) {
        return args.getOrDefault(key, defaultValue);
    }
}

