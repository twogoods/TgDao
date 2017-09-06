package com.tg.dao.generator.sql.primary;

import com.tg.dao.annotation.ModelConditions;
import com.tg.dao.annotation.Update;
import com.tg.dao.constant.Constants;
import com.tg.dao.constant.SqlMode;
import com.tg.dao.exception.TgDaoException;
import com.tg.dao.generator.sql.where.ModelWhereSqlGen;
import com.tg.dao.util.StringUtils;
import com.tg.dao.generator.model.TableMapping;
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
        if (StringUtils.isNotEmpty(update.columns())) {
            for (String column : update.columns().split(Constants.separator)) {
                String field = tableInfo.getColumnToField().get(column);
                if (StringUtils.isNotEmpty(field)) {
                    Element ifElement = setElement.addElement("if");
                    ifElement.addAttribute("test", field + " != null");
                    ifElement.addText(column + " = #{" + field + "},");
                }
            }
            return;
        }
        tableInfo.getFieldToColumn().forEach((field, column) -> {
            Element ifElement = setElement.addElement("if");
            ifElement.addAttribute("test", field + " != null");
            ifElement.addText(column + " = #{" + field + "},");
        });
    }
}
