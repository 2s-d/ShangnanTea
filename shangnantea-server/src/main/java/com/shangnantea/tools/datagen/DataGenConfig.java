package com.shangnantea.tools.datagen;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据生成规则配置的顶层模型，对应 YAML 中的：
 *
 * tables:
 *   users: {...}
 *   chat_messages: {...}
 */
public class DataGenConfig {

    private Map<String, TableRule> tables = new LinkedHashMap<>();

    public Map<String, TableRule> getTables() {
        return tables;
    }

    public void setTables(Map<String, TableRule> tables) {
        this.tables = tables;
    }
}

