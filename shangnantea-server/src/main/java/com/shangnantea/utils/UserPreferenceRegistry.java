package com.shangnantea.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户个人设置注册表。
 * 统一维护设置编号、数据库键名、值类型、默认值与可选约束。
 */
public enum UserPreferenceRegistry {

    THEME_MODE(
        101,
        "theme.mode",
        "theme",
        "themeMode",
        PreferenceValueType.STRING,
        "light",
        Arrays.asList("light", "dark", "auto"),
        null,
        null
    ),
    THEME_FONT_SIZE(
        102,
        "theme.fontSize",
        "theme",
        "fontSize",
        PreferenceValueType.INTEGER,
        "14",
        null,
        12,
        20
    ),
    THEME_FONT_FAMILY(
        103,
        "theme.fontFamily",
        "theme",
        "fontFamily",
        PreferenceValueType.STRING,
        "",
        null,
        null,
        null
    ),
    THEME_ENABLE_ANIMATION(
        104,
        "theme.enableAnimation",
        "theme",
        "enableAnimation",
        PreferenceValueType.BOOLEAN,
        "true",
        null,
        null,
        null
    ),
    DISPLAY_PROFILE_VISIBLE(
        201,
        "display.profileVisible",
        "display",
        "profileVisible",
        PreferenceValueType.BOOLEAN,
        "true",
        null,
        null,
        null
    );

    private static final Map<Integer, UserPreferenceRegistry> CODE_MAP = new HashMap<>();
    private static final Map<String, UserPreferenceRegistry> KEY_MAP = new HashMap<>();

    static {
        for (UserPreferenceRegistry item : values()) {
            CODE_MAP.put(item.code, item);
            KEY_MAP.put(item.key, item);
        }
    }

    private final Integer code;
    private final String key;
    private final String module;
    private final String fieldName;
    private final PreferenceValueType valueType;
    private final String defaultValue;
    private final List<String> allowedValues;
    private final Integer minValue;
    private final Integer maxValue;

    UserPreferenceRegistry(
        Integer code,
        String key,
        String module,
        String fieldName,
        PreferenceValueType valueType,
        String defaultValue,
        List<String> allowedValues,
        Integer minValue,
        Integer maxValue
    ) {
        this.code = code;
        this.key = key;
        this.module = module;
        this.fieldName = fieldName;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.allowedValues = allowedValues;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Integer getCode() {
        return code;
    }

    public String getKey() {
        return key;
    }

    public String getModule() {
        return module;
    }

    public String getFieldName() {
        return fieldName;
    }

    public PreferenceValueType getValueType() {
        return valueType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<String> getAllowedValues() {
        return allowedValues == null ? Collections.emptyList() : allowedValues;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public static UserPreferenceRegistry fromCode(Integer code) {
        return code == null ? null : CODE_MAP.get(code);
    }

    public static UserPreferenceRegistry fromKey(String key) {
        return key == null ? null : KEY_MAP.get(key);
    }

    /**
     * 将前端传入值归一化为数据库可存储的字符串值。
     *
     * @param rawValue 前端传入原始值
     * @return 归一化后的字符串
     */
    public String normalizeValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }

        switch (valueType) {
            case STRING:
                String stringValue = String.valueOf(rawValue);
                if (!getAllowedValues().isEmpty() && !getAllowedValues().contains(stringValue)) {
                    throw new IllegalArgumentException("invalid string value");
                }
                return stringValue;
            case INTEGER:
                Integer intValue = normalizeIntegerValue(rawValue);
                if (minValue != null && intValue < minValue) {
                    throw new IllegalArgumentException("integer value below minimum");
                }
                if (maxValue != null && intValue > maxValue) {
                    throw new IllegalArgumentException("integer value above maximum");
                }
                return String.valueOf(intValue);
            case BOOLEAN:
                Boolean booleanValue = normalizeBooleanValue(rawValue);
                return String.valueOf(booleanValue);
            default:
                throw new IllegalArgumentException("unsupported preference value type");
        }
    }

    /**
     * 将数据库值转换为前端使用的类型。
     *
     * @param storedValue 数据库存储值
     * @return 转换后的对象
     */
    public Object parseStoredValue(String storedValue) {
        String value = storedValue == null ? defaultValue : storedValue;
        switch (valueType) {
            case STRING:
                return value;
            case INTEGER:
                return Integer.parseInt(value);
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            default:
                return value;
        }
    }

    private Integer normalizeIntegerValue(Object rawValue) {
        if (rawValue instanceof Number) {
            return ((Number) rawValue).intValue();
        }
        return Integer.parseInt(String.valueOf(rawValue).trim());
    }

    private Boolean normalizeBooleanValue(Object rawValue) {
        if (rawValue instanceof Boolean) {
            return (Boolean) rawValue;
        }
        String value = String.valueOf(rawValue).trim().toLowerCase();
        if ("true".equals(value) || "1".equals(value) || "yes".equals(value)) {
            return true;
        }
        if ("false".equals(value) || "0".equals(value) || "no".equals(value)) {
            return false;
        }
        throw new IllegalArgumentException("invalid boolean value");
    }

    /**
     * 用户设置值类型。
     */
    public enum PreferenceValueType {
        STRING("string"),
        INTEGER("number"),
        BOOLEAN("boolean");

        private final String dbType;

        PreferenceValueType(String dbType) {
            this.dbType = dbType;
        }

        public String getDbType() {
            return dbType;
        }
    }
}
