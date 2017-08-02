package com.tg.generator.sql.primary;

import com.tg.annotation.Count;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public class CountGen extends SelectGen {
    private Count count;

    public CountGen(ExecutableElement executableElement, TableMapping tableInfo, Count count) {
        super(executableElement, tableInfo, null);
        this.count = count;
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element selectElement = root.addElement("select");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString());
        selectElement.addAttribute("resultType", executableElement.getReturnType().getKind().toString());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select count(*)").append(" from ");
        sqlBuilder.append(tableInfo.getTableName()).append(StringUtils.BLANK);
        selectElement.addText(sqlBuilder.toString());
        return selectElement;
    }
}
