package com.tg.dao.generator.sql;

import com.tg.dao.annotation.Limit;
import com.tg.dao.annotation.OffSet;
import com.tg.dao.annotation.Params;
import com.tg.dao.exception.TgDaoException;
import com.tg.dao.generator.model.TableMapping;
import com.tg.dao.util.StringUtils;
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

    protected void checkOneParam(){
        if (executableElement.getParameters().size() != 1) {
            throw new TgDaoException(String.format("check method %s , support only one parameter", executableElement.getSimpleName().toString()));
        }
    }

    protected boolean paramsAnnotated(VariableElement variableElement) {
        if (variableElement == null) {
            return false;
        }
        //check paramter
        if (variableElement.getAnnotation(Params.class) != null) {
            return true;
        }
        //check method
        if (executableElement.getAnnotation(Params.class) != null) {
            return true;
        }
        //check class
        if (executableElement.getEnclosingElement().getAnnotation(Params.class) != null) {
            return true;
        }
        return false;
    }

    protected boolean commonNameForEach(VariableElement variableElement) {
        if (executableElement.getParameters().size() == 1) {
            return !paramsAnnotated(variableElement);
        }
        return false;
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

    protected String getField(String param) {
        if (tableInfo.getColumnToField().containsKey(param)) {
            return tableInfo.getColumnToField().get(param);
        }
        return param;
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
