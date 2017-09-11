package com.tg.dao.generator.sql.primary;

import com.tg.dao.annotation.BatchInsert;
import com.tg.dao.constant.Constants;
import com.tg.dao.exception.TgDaoException;
import com.tg.dao.generator.model.TableMapping;
import com.tg.dao.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        sqlPrefix.append("insert into ").append(tableInfo.getTableName()).append(Constants.BLANK);
        String columns = batchInsert.columns();

        if (StringUtils.isEmpty(columns)) {
            sqlPrefix.append("(");
            List<String> columnList = new ArrayList<>();
            tableInfo.getColumnToField().forEach((key, value) -> {
                sqlPrefix.append(key).append(",");
                columnList.add(key);
            });
            sqlPrefix.deleteCharAt(sqlPrefix.lastIndexOf(",")).append(")");
            insertElement.addText(sqlPrefix.append(" values ").toString());
            generateEach(insertElement, columnList);
        } else {
            sqlPrefix.append("(").append(columns).append(")").append(" values ");
            insertElement.addText(sqlPrefix.toString());
            generateEach(insertElement, columns);
        }
        return null;
    }

    private void generateEach(Element sqlElement, String columns) {
        generateEach(sqlElement, Arrays.asList(columns.split(Constants.separator)));
    }

    private void generateEach(Element sqlElement, List<String> columns) {
        Element each = sqlElement.addElement("foreach");
        each.addAttribute("collection", "collection");
        each.addAttribute("item", "item");
        each.addAttribute("separator", Constants.separator);
        StringBuilder eachSql = new StringBuilder().append("(");
        columns.forEach(column -> eachSql.append("#{item.").append(getField(column)).append("},"));
        eachSql.deleteCharAt(eachSql.lastIndexOf(",")).append(")");
        each.addText(eachSql.toString());
    }
}
