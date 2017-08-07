package com.tg.generator.sql.primary;

import com.tg.annotation.ModelConditions;
import com.tg.annotation.Update;
import com.tg.constant.SqlMode;
import com.tg.exception.TgDaoException;
import com.tg.generator.model.TableMapping;
import com.tg.generator.sql.where.ModelWhereSqlGen;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/1.
 */
public class UpdateGen extends PrimarySqlGen {
    private Update update;

    public UpdateGen(ExecutableElement executableElement, TableMapping tableInfo, Update update) {
        super(executableElement, tableInfo);
        this.update = update;
    }

    @Override
    protected void checkAnnotatedRule() {
        //TODO 多参数问题
        if (executableElement.getParameters().size() != 1) {
            throw new TgDaoException(String.format("check method %s , support only one parameter", executableElement.getSimpleName().toString()));
        }
        ModelConditions modelConditions = executableElement.getAnnotation(ModelConditions.class);
        if (modelConditions == null) {

        } else {
            whereSqlGen = new ModelWhereSqlGen(executableElement, tableInfo, SqlMode.COMMON, modelConditions);
        }
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element updateElement = root.addElement("update");
        updateElement.addAttribute("id", executableElement.getSimpleName().toString());
        updateElement.addText("update " + tableInfo.getTableName());
        generateSet(updateElement);
        return updateElement;
    }

    private void generateSet(Element updateElement) {
        Element setElement = updateElement.addElement("set");
        tableInfo.getFieldToColumn().forEach((field, column) -> {
            Element ifElement = setElement.addElement("if");
            ifElement.addAttribute("test", field + " != null");
            ifElement.addText(column + " = #{" + field + "},");
        });
    }
}
