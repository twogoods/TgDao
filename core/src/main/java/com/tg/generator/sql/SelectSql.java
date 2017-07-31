package com.tg.generator.sql;

import com.tg.annotation.Limit;
import com.tg.annotation.OffSet;
import com.tg.annotation.OrderBy;
import com.tg.annotation.Select;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public class SelectSql extends SqlGen {
    private Select select;

    public SelectSql(ExecutableElement executableElement, TableMapping tableInfo, Select select) {
        super(executableElement, tableInfo);
        this.select = select;
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element selectElement = root.addElement("select");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString());
        //TODO 无paramType
        selectElement.addAttribute("resultType", tableInfo.getClassName());
        StringBuilder sqlBuilder = new StringBuilder();
        String columns = select.columns();
        if (StringUtils.isEmpty(columns)) {
            sqlBuilder.append("select * from ");
        } else {
            sqlBuilder.append("select ").append(columns).append(" from ");
        }
        sqlBuilder.append(tableInfo.getTableName()).append(StringUtils.BLANK);
        selectElement.addText(sqlBuilder.toString());
        return selectElement;
    }

    @Override
    protected void generateWhereSql(Element sqlElement) {
        if (variableElements.size() == 0) {
            return;
        }
        Element whereElement = sqlElement.addElement("where");
        for (int i = 0; i < variableElements.size(); i++) {
            generateWhereParams(variableElements.get(i), whereElement, i);
        }
    }

    @Override
    protected void generateOrderAndPage(Element sqlElement) {
        OrderBy orderBy = executableElement.getAnnotation(OrderBy.class);
        if (orderBy != null) {
            sqlElement.addText(" order by " + orderBy.value());
        }
        Limit limit = null;
        int limitIndex = 0;
        OffSet offSet = null;
        int offsetIndex = 0;
        for (int i = 0; i < variableElements.size(); i++) {
            if (limit == null && (limit = variableElements.get(i).getAnnotation(Limit.class)) != null) {
                limitIndex = i;
                continue;
            }
            if (offSet == null && (offSet = variableElements.get(i).getAnnotation(OffSet.class)) != null) {
                offsetIndex = i;
            }
        }
        if (limit != null && offSet != null) {
            sqlElement.addText(" limit #{" + offsetIndex + "}, #{" + limitIndex + "}");
        } else if (limit != null) {
            sqlElement.addText(" limit #{" + limitIndex + "}");
        }
    }
}
