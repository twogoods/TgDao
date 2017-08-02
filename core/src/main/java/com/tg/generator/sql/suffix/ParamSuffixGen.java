package com.tg.generator.sql.suffix;

import com.tg.annotation.Limit;
import com.tg.annotation.OffSet;
import com.tg.generator.model.TableMapping;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/2.
 */
public class ParamSuffixGen extends AbstractSuffixSqlGen {
    public ParamSuffixGen(ExecutableElement executableElement, TableMapping tableInfo) {
        super(executableElement, tableInfo);
    }

    @Override
    public void generatePage(Element sqlElement) {
        Limit limit = null;
        int limitIndex = 0;
        OffSet offSet = null;
        int offsetIndex = 0;
        for (int i = 0; i < variableElements.size(); i++) {
            if (limit == null && (limit = variableElements.get(i).getAnnotation(Limit.class)) != null) {
                limitIndex = i;
                continue;
            }
            if (offSet == null && (offSet = variableElements.get(i).getAnnotation(OffSet.class)) != null) {
                offsetIndex = i;
            }
        }
        if (limit != null && offSet != null) {
            sqlElement.addText(" limit #{" + offsetIndex + "}, #{" + limitIndex + "}");
        } else if (limit != null) {
            sqlElement.addText(" limit #{" + limitIndex + "}");
        }
    }
}
