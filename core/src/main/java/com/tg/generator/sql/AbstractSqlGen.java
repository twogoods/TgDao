package com.tg.generator.sql;

import com.tg.annotation.Condition;
import com.tg.annotation.Limit;
import com.tg.annotation.OffSet;
import com.tg.constant.SqlMode;
import com.tg.generator.model.TableMapping;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

/**
 * Created by twogoods on 2017/7/31.
 */
public abstract class AbstractSqlGen {
    protected ExecutableElement executableElement;
    protected TableMapping tableInfo;
    protected List<? extends VariableElement> variableElements;

    public AbstractSqlGen(ExecutableElement executableElement, TableMapping tableInfo) {
        this.executableElement = executableElement;
        this.tableInfo = tableInfo;
        this.variableElements = executableElement.getParameters();
    }

    protected void generateWhereParamsSelective(VariableElement variableElement, Element whereElement, int index) {
        //TODO 解决mybatis @Param() 的问题
    }

    protected boolean isPageParam(VariableElement variableElement) {
        boolean flag = variableElement.getAnnotation(Limit.class) != null || variableElement.getAnnotation(OffSet.class) != null;
        return flag;
    }

    protected Element generateTrimElement(Element element, String prefix, String suffix, String suffixOverrides) {
        Element trimElement = element.addElement("trim")
                .addAttribute("prefix", prefix)
                .addAttribute("suffix", suffix)
                .addAttribute("suffixOverrides", suffixOverrides);
        return trimElement;
    }

    protected void generateEach(Element sqlElement, String columns, String varName) {
        Element each = sqlElement.addElement("foreach");
        each.addAttribute("collection", varName);
        each.addAttribute("item", "item");
        each.addAttribute("separator", ",");
        StringBuilder eachSql = new StringBuilder().append("(");
        String[] columnArray = columns.split(",");
        for (String column : columnArray) {
            eachSql.append("#{item.").append(tableInfo.getColumnToField().get(column)).append("},");
        }
        eachSql.deleteCharAt(eachSql.lastIndexOf(",")).append(")");
        each.addText(eachSql.toString());
    }
}
