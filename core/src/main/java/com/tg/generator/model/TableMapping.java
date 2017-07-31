package com.tg.generator.model;

import java.util.Map;

/**
 * Created by twogoods on 2017/7/28.
 */

public class TableMapping {
    private String tableName;
    private String className;
    private String idColumn;
    private String idField;
    private Map<String, String> fieldToColumn;
    private Map<String, String> columnToField;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, String> getFieldToColumn() {
        return fieldToColumn;
    }

    public void setFieldToColumn(Map<String, String> fieldToColumn) {
        this.fieldToColumn = fieldToColumn;
    }

    public Map<String, String> getColumnToField() {
        return columnToField;
    }

    public void setColumnToField(Map<String, String> columnToField) {
        this.columnToField = columnToField;
    }
}
