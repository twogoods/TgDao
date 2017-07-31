package com.tg.generator.sql;

import com.tg.annotation.BatchInsert;
import com.tg.annotation.Insert;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public class BatchInsertSql extends SqlGen {
    private BatchInsert batchInsert;

    public BatchInsertSql(ExecutableElement executableElement, Element root, TableMapping tableInfo, BatchInsert batchInsert) {
        super(executableElement, root, tableInfo);
        this.batchInsert = batchInsert;
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element selectElement = root.addElement("insert");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString())
                .addAttribute("parameterType", tableInfo.getClassName());
        StringBuilder sqlPrefix = new StringBuilder();
        StringBuilder sqlSuffix = new StringBuilder();
        sqlPrefix.append("insert into ").append(tableInfo.getTableName()).append(StringUtils.BLANK);
        sqlSuffix.append(" values ");
        String columns = batchInsert.columns();

        if (StringUtils.isEmpty(columns)) {
            sqlPrefix.append("(");
            sqlSuffix.append("(");
            tableInfo.getColumnToField().forEach((key, value) -> {
                sqlPrefix.append(key).append(",");
                sqlSuffix.append("#{").append(value).append("},");
            });
            sqlPrefix.deleteCharAt(sqlPrefix.lastIndexOf(",")).append(")");
            sqlSuffix.deleteCharAt(sqlSuffix.lastIndexOf(",")).append(")");
            selectElement.addText(sqlPrefix.append(sqlSuffix.toString()).toString());
        } else {
            String[] columnArray = columns.split(",");
            for (String column : columnArray) {
                sqlSuffix.append("#{").append(tableInfo.getColumnToField().get(column)).append("},");
            }
        }
        return null;
    }

    @Override
    protected void generateWhereSql(Element sqlElement) {

    }

    @Override
    protected void generateOrderAndPage(Element sqlElement) {

    }
}
