package com.tg.generator.sql.where;

import com.tg.annotation.ModelCondition;
import com.tg.annotation.ModelConditions;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/2.
 */
public class ModelWhereSqlGen extends AbstractWhereSqlGen {
    private ModelConditions modelConditions;

    public ModelWhereSqlGen(ExecutableElement executableElement, TableMapping tableInfo, ModelConditions modelConditions) {
        super(executableElement, tableInfo);
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
        Element ifElement = whereElement.addElement("if");
        ifElement.addAttribute("test", modelCondition.field() + " != null");
        ifElement.addText(modelCondition.attach().name() + StringUtils.BLANK +
                modelCondition.field() + StringUtils.BLANK + modelCondition.criterion().getCriterion() +
                "#{" + modelCondition.field() + "}");
    }
}
