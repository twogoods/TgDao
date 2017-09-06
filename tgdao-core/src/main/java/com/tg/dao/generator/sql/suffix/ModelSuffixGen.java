package com.tg.dao.generator.sql.suffix;

import com.tg.dao.annotation.Page;
import com.tg.dao.generator.model.TableMapping;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

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
        if (page != null) {
            sqlElement.addText(" limit #{" + page.offsetField() + "}, #{" + page.limitField() + "}");
        }
    }
}
