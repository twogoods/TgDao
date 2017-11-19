package com.tg.dao.generator.sql.suffix;

import com.tg.dao.annotation.Page;
import com.tg.dao.generator.model.TableMapping;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by twogoods on 2017/8/2.
 */
public class ModelSuffixGen extends AbstractSuffixSqlGen {
    public ModelSuffixGen(ExecutableElement executableElement, TableMapping tableInfo) {
        super(executableElement, tableInfo);
    }

    @Override
    public void generatePage(Element sqlElement) {
        Page page = executableElement.getAnnotation(Page.class);
        VariableElement variableElement = variableElements.get(0);
        String offset = page.offsetField();
        String limit = page.limitField();
        if (paramsAnnotated(variableElement)) {
            offset = variableElement.getSimpleName().toString() + "." + offset;
            limit = variableElement.getSimpleName().toString() + "." + limit;
        }
        if (page != null) {
            sqlElement.addText(String.format(" limit #{ %s }, #{ %s }", offset, limit));
        }
    }
}
