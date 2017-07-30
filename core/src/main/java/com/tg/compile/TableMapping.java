package com.tg.compile;

import java.util.List;
import java.util.Map;

/**
 * Created by twogoods on 2017/7/28.
 */

public class TableMapping {
    private String tableName;
    private String className;
    private List<String> columns;
    private Map<String, String> fieldToColumn;
    private Map<String, String> columnToField;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
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
