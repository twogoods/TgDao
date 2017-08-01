package com.tg.generator.sql;

import com.tg.annotation.Limit;
import com.tg.annotation.OffSet;
import com.tg.annotation.OrderBy;
import com.tg.annotation.Select;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public class SelectSql extends SqlGen {
    private Select select;

    public SelectSql(ExecutableElement executableElement, TableMapping tableInfo, Select select) {
        super(executableElement, tableInfo);
        this.select = select;
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element selectElement = root.addElement("select");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString());
        //TODO 无paramType
        selectElement.addAttribute("resultType", tableInfo.getClassName());
        StringBuilder sqlBuilder = new StringBuilder();
        String columns = select.columns();
        if (StringUtils.isEmpty(columns)) {
            sqlBuilder.append("select * from ");
        } else {
            sqlBuilder.append("select ").append(columns).append(" from ");
        }
        sqlBuilder.append(tableInfo.getTableName()).append(StringUtils.BLANK);
        selectElement.addText(sqlBuilder.toString());
        return selectElement;
    }

    @Override
    protected void generateOrderAndPage(Element sqlElement) {
        commonWhereSql(sqlElement);
    }

    @Override
    protected void generateWhereSql(Element sqlElement) {
        commonOrderAndPage(sqlElement);
    }
}
