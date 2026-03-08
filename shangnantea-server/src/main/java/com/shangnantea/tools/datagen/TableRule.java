package com.shangnantea.tools.datagen;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单张表的数据生成规则。
 */
public class TableRule {

    /**
     * 默认生成条数
     */
    private int defaultCount = 100;

    /**
     * 字段规则：字段名 -> 规则
     */
    private Map<String, FieldRule> fields = new LinkedHashMap<>();

    /**
     * 关联表配置：表名 -> 关联规则
     * 例如：生成10个店铺时，每个店铺自动生成2张轮播图
     * relatedTables:
     *   shop_banners:
     *     count: 2  # 每个店铺生成2张轮播图
     */
    private Map<String, RelatedTableRule> relatedTables = new LinkedHashMap<>();

    public int getDefaultCount() {
        return defaultCount;
    }

    public void setDefaultCount(int defaultCount) {
        this.defaultCount = defaultCount;
    }

    public Map<String, FieldRule> getFields() {
        return fields;
    }

    public void setFields(Map<String, FieldRule> fields) {
        this.fields = fields;
    }

    public Map<String, RelatedTableRule> getRelatedTables() {
        return relatedTables;
    }

    public void setRelatedTables(Map<String, RelatedTableRule> relatedTables) {
        this.relatedTables = relatedTables;
    }
}

