package com.tg.generator.sql.where;

import com.tg.annotation.ModelCondition;
import com.tg.annotation.ModelConditions;
import com.tg.constant.SqlMode;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/2.
 */
public class ModelWhereSqlGen extends AbstractWhereSqlGen {
    private ModelConditions modelConditions;

    public ModelWhereSqlGen(ExecutableElement executableElement, TableMapping tableInfo, SqlMode sqlMode, ModelConditions modelConditions) {
        super(executableElement, tableInfo, sqlMode);
        this.modelConditions = modelConditions;
    }

    @Override
    public void generateWhereSql(Element sqlElement) {
        ModelCondition[] conditions = modelConditions.value();
        if (conditions.length == 0) {
            return;
        }
        Element whereElement = sqlElement.addElement("where");
        for (ModelCondition modelCondition : conditions) {
            generateModelWhereParam(whereElement, modelCondition);
        }
    }

    private void generateModelWhereParam(Element whereElement, ModelCondition modelCondition) {
        if (sqlMode == SqlMode.SELECTIVE) {
            Element ifElement = whereElement.addElement("if");
            ifElement.addAttribute("test", modelCondition.field() + " != null");
            ifElement.addText(modelCondition.attach().name() + StringUtils.BLANK +
                    getColumn(modelCondition,modelCondition.field()) + StringUtils.BLANK + modelCondition.criterion().getCriterion() +
                    "#{" + modelCondition.field() + "}");
        } else {
            whereElement.addText(modelCondition.attach().name() + StringUtils.BLANK +
                    getColumn(modelCondition,modelCondition.field()) + StringUtils.BLANK + modelCondition.criterion().getCriterion() +
                    "#{" + modelCondition.field() + "}");
        }
    }

    private String getColumn(ModelCondition modelCondition, String field) {
        String column = modelCondition.column();
        if (StringUtils.isEmpty(column)) {
            column = tableInfo.getFieldToColumn().get(field);
            return StringUtils.isEmpty(column) ? field : column;
        }
        return column;
    }
}
