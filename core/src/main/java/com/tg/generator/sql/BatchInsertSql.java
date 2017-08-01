package com.tg.generator.sql;

import com.tg.annotation.BatchInsert;
import com.tg.annotation.Insert;
import com.tg.constant.Criterions;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public class BatchInsertSql extends SqlGen {
    private BatchInsert batchInsert;

    public BatchInsertSql(ExecutableElement executableElement, TableMapping tableInfo, BatchInsert batchInsert) {
        super(executableElement, tableInfo);
        this.batchInsert = batchInsert;
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element insertElement = root.addElement("insert");
        insertElement.addAttribute("id", executableElement.getSimpleName().toString())
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
            insertElement.addText(sqlPrefix.append(sqlSuffix.toString()).toString());
        } else {
            sqlPrefix.append("(").append(columns).append(")").append(sqlSuffix.toString());
            insertElement.addText(sqlPrefix.toString());
            generateEach(insertElement, columns, variableElements.get(0).getSimpleName().toString());
        }
        return null;
    }


    private void generateEach(Element sqlElement, String columns, String varName) {
        Element each = sqlElement.addElement("foreach");
        each.addAttribute("collection", varName);
        each.addAttribute("item", "item");
        each.addAttribute("separator", ",");
        StringBuilder eachSql = new StringBuilder().append("(");
        String[] columnArray = columns.split(",");
        for (String column : columnArray) {
            eachSql.append("#{item.").append(tableInfo.getColumnToField().get(column)).append("},");
        }
        eachSql.deleteCharAt(eachSql.lastIndexOf(",")).append(")");
        each.addText(eachSql.toString());
    }

    @Override
    protected void generateOrderAndPage(Element sqlElement) {

    }

    @Override
    protected void generateWhereSql(Element sqlElement) {

    }
}
