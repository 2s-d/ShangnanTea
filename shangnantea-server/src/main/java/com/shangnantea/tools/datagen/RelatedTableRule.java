package com.shangnantea.tools.datagen;

/**
 * 关联表生成规则。
 * 用于配置在主表生成数据后，自动生成关联表的数据。
 * 
 * 例如：生成10个店铺时，每个店铺自动生成2张轮播图
 */
public class RelatedTableRule {

    /**
     * 每个主表记录生成多少条关联表记录
     */
    private int count = 1;

    /**
     * 关联字段映射：主表字段 -> 关联表字段
     * 例如：shop_id -> shop_id（店铺ID映射到轮播图的shop_id）
     */
    private java.util.Map<String, String> fieldMapping = new java.util.LinkedHashMap<>();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public java.util.Map<String, String> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(java.util.Map<String, String> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }
}
