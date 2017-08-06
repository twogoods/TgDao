package com.tg.generator.sql.primary;

import com.tg.annotation.BatchInsert;
import com.tg.exception.TgDaoException;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public class BatchInsertGen extends PrimarySqlGen {
    private BatchInsert batchInsert;

    public BatchInsertGen(ExecutableElement executableElement, TableMapping tableInfo, BatchInsert batchInsert) {
        super(executableElement, tableInfo);
        this.batchInsert = batchInsert;
    }

    @Override
    protected void checkAnnotatedRule() {
        if (executableElement.getParameters().size() != 1) {
            throw new TgDaoException(String.format("check method %s , support only one parameter",
                    executableElement.getSimpleName().toString()));
        }
        if (!executableElement.getParameters().get(0).asType().toString().contains(tableInfo.getClassName())) {
            throw new TgDaoException(String.format("insert param need '%s' but get '%s'", tableInfo.getClassName(),
                    executableElement.getParameters().get(0).asType().toString()));
        }
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
            generateEach(insertElement, columns);
        }
        return null;
    }

    private void generateEach(Element sqlElement, String columns) {
        Element each = sqlElement.addElement("foreach");
        each.addAttribute("collection", "collection");
        each.addAttribute("item", "item");
        each.addAttribute("separator", ",");
        StringBuilder eachSql = new StringBuilder().append("(");
        String[] columnArray = columns.split(",");
        for (String column : columnArray) {
            eachSql.append("#{item.").append(getColumn(column)).append("},");
        }
        eachSql.deleteCharAt(eachSql.lastIndexOf(",")).append(")");
        each.addText(eachSql.toString());
    }
}
