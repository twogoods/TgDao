package com.tg.dao.generator.sql.primary;

import com.tg.dao.annotation.Insert;
import com.tg.dao.constant.Constants;
import com.tg.dao.exception.TgDaoException;
import com.tg.dao.generator.model.TableMapping;
import com.tg.dao.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Map;

/**
 * Created by twogoods on 2017/7/31.
 */
public class InsertGen extends PrimarySqlGen {
    private Insert insert;

    public InsertGen(ExecutableElement executableElement, TableMapping tableInfo, Insert insert) {
        super(executableElement, tableInfo);
        this.insert = insert;
    }

    @Override
    public void checkAnnotatedRule() {
        checkOneParam();
        if (!tableInfo.getClassName().equals(executableElement.getParameters().get(0).asType().toString())) {
            throw new TgDaoException(String.format("insert param need '%s' but get '%s'", tableInfo.getClassName(),
                    executableElement.getParameters().get(0).asType().toString()));
        }
    }

    @Override
    public Element generateBaseSql(Element root) {
        Element selectElement = root.addElement("insert");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString())
                .addAttribute("parameterType", tableInfo.getClassName());
        if (insert.useGeneratedKeys() && StringUtils.isNotEmpty(insert.keyProperty())) {
            selectElement.addAttribute("useGeneratedKeys", "true")
                    .addAttribute("keyProperty", insert.keyProperty());
        }
        selectElement.addText("insert into " + tableInfo.getTableName());
        Element columnElement = generateTrimElement(selectElement, "(", ")", ",");
        Element valuesElement = generateTrimElement(selectElement, "values (", ")", ",");

        VariableElement variableElement = variableElements.get(0);
        String objName = "";
        if (paramsAnnotated(variableElement)) {
            objName = variableElement.getSimpleName().toString() + ".";
        }
        if (StringUtils.isNotEmpty(insert.columns())) {
            for (String column : insert.columns().split(Constants.separator)) {
                String field = tableInfo.getColumnToField().get(column);
                if (StringUtils.isNotEmpty(field)) {
                    columnElement.addElement("if")
                            .addAttribute("test", objName + field + " != null")
                            .addText(column + Constants.separator);
                    valuesElement.addElement("if")
                            .addAttribute("test", objName + field + " != null")
                            .addText("#{" + field + "},");
                }
            }
            return selectElement;
        }

        for (Map.Entry<String, String> entry : tableInfo.getFieldToColumn().entrySet()) {
            columnElement.addElement("if")
                    .addAttribute("test", objName + entry.getKey() + " != null")
                    .addText(entry.getValue() + Constants.separator);
            valuesElement.addElement("if")
                    .addAttribute("test", objName + entry.getKey() + " != null")
                    .addText("#{" + objName + entry.getKey() + "},");
        }
        return selectElement;
    }
}
