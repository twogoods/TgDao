package com.tg.generator.sql;

import com.tg.annotation.Limit;
import com.tg.annotation.OffSet;
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

    protected String getColumn(String param) {
        if (tableInfo.getColumnToField().containsKey(param)) {
            return param;
        }
        if (tableInfo.getFieldToColumn().containsKey(param)) {
            return tableInfo.getFieldToColumn().get(param);
        }
        return param;
    }

    protected String getColumn(String column, String varName) {
        return StringUtils.isEmpty(column) ? getColumn(varName) : column;
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
}
