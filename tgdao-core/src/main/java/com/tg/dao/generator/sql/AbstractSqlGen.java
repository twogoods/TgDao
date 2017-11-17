package com.tg.dao.generator.sql;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.SymbolMetadata;
import com.tg.dao.annotation.Limit;
import com.tg.dao.annotation.OffSet;
import com.tg.dao.annotation.Params;
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
    private static final String PARAMS_ANNOTATION = Params.class.getCanonicalName();

    protected ExecutableElement executableElement;
    protected TableMapping tableInfo;
    protected List<? extends VariableElement> variableElements;

    public AbstractSqlGen(ExecutableElement executableElement, TableMapping tableInfo) {
        this.executableElement = executableElement;
        this.tableInfo = tableInfo;
        this.variableElements = executableElement.getParameters();
    }


    protected boolean paramAnnotated(VariableElement variableElement) {
        if (variableElement == null) {
            return false;
        }
        //检查参数
        Symbol.VarSymbol varSymbol = (Symbol.VarSymbol) variableElement;
        SymbolMetadata symbolMetadata = varSymbol.getMetadata();
        if (symbolMetadata != null) {
            com.sun.tools.javac.util.List<Attribute.Compound> compounds = symbolMetadata.getDeclarationAttributes();
            for (Attribute.Compound compound : compounds) {
                if ("org.apache.ibatis.annotations.Param".equals(compound.getAnnotationType().toString())) {
                    return true;
                }
            }
        }
        //检查方法
        Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) executableElement;
        for (Attribute.Compound compound : methodSymbol.getMetadata().getDeclarationAttributes()) {
            if (PARAMS_ANNOTATION.equals(compound.getAnnotationType().toString())) {
                return true;
            }
        }
        //检查类
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) methodSymbol.getEnclosingElement();
        for (Attribute.Compound compound : classSymbol.getMetadata().getDeclarationAttributes()) {
            if (PARAMS_ANNOTATION.equals(compound.getAnnotationType().toString())) {
                return true;
            }
        }
        return false;
    }

    protected boolean commonNameForEach(VariableElement variableElement) {
        if (executableElement.getParameters().size() == 1) {
            return !paramAnnotated(variableElement);
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
