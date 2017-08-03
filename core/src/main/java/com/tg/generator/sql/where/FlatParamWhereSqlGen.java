package com.tg.generator.sql.where;

import com.tg.annotation.Condition;
import com.tg.constant.Attach;
import com.tg.constant.Criterions;
import com.tg.constant.SqlMode;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by twogoods on 2017/8/2.
 */
public class FlatParamWhereSqlGen extends AbstractWhereSqlGen {

    public FlatParamWhereSqlGen(ExecutableElement executableElement, TableMapping tableInfo, SqlMode sqlMode) {
        super(executableElement, tableInfo, sqlMode);
    }

    @Override
    public void generateWhereSql(Element sqlElement) {
        if (variableElements.size() == 0) {
            return;
        }
        Element whereElement = sqlElement.addElement("where");
        for (int i = 0; i < variableElements.size(); i++) {
            generateWhereParam(variableElements.get(i), whereElement, i);
        }
    }

    private void generateWhereParam(VariableElement variableElement, Element whereElement, int index) {
        if (isPageParam(variableElement)) {
            return;
        }
        String varName = variableElement.getSimpleName().toString();
        Condition condition = variableElement.getAnnotation(Condition.class);
        StringBuilder sqlBuilder = new StringBuilder();
        if (condition == null) {
            sqlBuilder.append(Attach.AND.name())
                    .append(StringUtils.BLANK)
                    .append(getColumn(condition, varName))
                    .append(" = ")
                    .append("#{").append(index).append("} ");
            return;
        }
        Criterions criterion = condition.value();
        if (criterion.inCriterion()) {
            generateINSuffix(criterion, whereElement, StringUtils.isEmpty(condition.column()) ? varName : condition.column(), varName);
        } else {
            sqlBuilder.append(condition.attach().name())
                    .append(StringUtils.BLANK)
                    .append(getColumn(condition, varName))
                    .append(StringUtils.BLANK)
                    .append(criterion.getCriterion())
                    .append(" #{").append(index).append("} ");
            whereElement.addText(sqlBuilder.toString());
        }
    }

    private String getColumn(Condition condition, String varName) {
        String column = null;
        if (condition == null) {
            column = tableInfo.getFieldToColumn().get(varName);
            return StringUtils.isEmpty(column) ? varName : column;
        }
        return StringUtils.isEmpty(condition.column()) ? varName : condition.column();
    }
}
