package com.tg.generator.sql.where;

import com.tg.annotation.Condition;
import com.tg.constant.Attach;
import com.tg.constant.Criterions;
import com.tg.constant.SqlMode;
import com.tg.generator.model.TableMapping;
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
        variableElements.forEach(variableElement -> generateWhereParam(variableElement, whereElement));
    }

    private void generateWhereParam(VariableElement variableElement, Element whereElement) {
        if (isPageParam(variableElement)) {
            return;
        }
        String varName = variableElement.getSimpleName().toString();
        Condition condition = variableElement.getAnnotation(Condition.class);
        if (condition == null) {
            whereParamSqlGen.generateWhereParamSql(whereElement, Criterions.EQUAL, Attach.AND, getColumn(varName), varName);
            return;
        }
        if (condition.value().inCriterion()) {
            generateINSuffix(condition, whereElement, variableElement);
        } else {
            whereParamSqlGen.generateWhereParamSql(whereElement, condition.value(), condition.attach(), getColumn(condition.column(), varName), varName);
        }
    }
}
