package com.tg.generator.sql.suffix;

import com.tg.generator.model.TableMapping;
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
        sqlElement.addText(" limit #{offset}, #{limit}");
    }
}