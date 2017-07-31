package com.tg.generator.sql;

import com.tg.annotation.Count;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public class SelectCountSql extends SelectSql {
    private Count count;

    public SelectCountSql(ExecutableElement executableElement, Element root, TableMapping tableInfo, Count count) {
        super(executableElement, root, tableInfo, null);
        this.count = count;
    }

    @Override
    public Element generateBaseSql(Element root) {
        Element selectElement = root.addElement("select");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString());
        selectElement.addAttribute("resultType", executableElement.getReturnType().getKind().toString());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select count(*)").append(" from ");
        sqlBuilder.append(tableInfo.getTableName()).append(StringUtils.BLANK);
        selectElement.addText(sqlBuilder.toString());
        return selectElement;
    }

    @Override
    protected void generateOrderAndPage(Element sqlElement) {
    }
}
