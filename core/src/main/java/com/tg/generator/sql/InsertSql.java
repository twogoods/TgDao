package com.tg.generator.sql;

import com.tg.annotation.Insert;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public class InsertSql extends SqlGen {
    private Insert insert;

    public InsertSql(ExecutableElement executableElement, Element root, TableMapping tableInfo, Insert insert) {
        super(executableElement, root, tableInfo);
        this.insert = insert;
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element selectElement = root.addElement("insert");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString())
                .addAttribute("parameterType", tableInfo.getClassName());
        if (insert.useGeneratedKeys() && StringUtils.isEmpty(insert.keyProperty())) {
            selectElement.addAttribute("useGeneratedKeys", "true")
                    .addAttribute("keyProperty", insert.keyProperty());
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("insert into ").append(tableInfo.getTableName()).append(StringUtils.BLANK);
        Element columnElement = generateTrimElement(root);
        Element valuesElement = generateTrimElement(root);
        tableInfo.getFieldToColumn().forEach((key, value) -> {
            columnElement.addElement("if")
                    .addAttribute("test", key + " != null")
                    .addText(value + ",");
            valuesElement.addElement("if")
                    .addAttribute("test", key + " != null")
                    .addText("#{" + key + "},");

        });
        selectElement.addText(sqlBuilder.toString());
        return null;
    }

    @Override
    protected void generateWhereSql(Element sqlElement) {

    }

    @Override
    protected void generateOrderAndPage(Element sqlElement) {

    }
}
