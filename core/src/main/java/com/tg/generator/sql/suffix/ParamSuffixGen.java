package com.tg.generator.sql.suffix;

import com.tg.annotation.Limit;
import com.tg.annotation.OffSet;
import com.tg.generator.model.TableMapping;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Optional;

/**
 * Created by twogoods on 2017/8/2.
 */
public class ParamSuffixGen extends AbstractSuffixSqlGen {
    public ParamSuffixGen(ExecutableElement executableElement, TableMapping tableInfo) {
        super(executableElement, tableInfo);
    }

    @Override
    public void generatePage(Element sqlElement) {
        Optional<? extends VariableElement> limitVariableElement = variableElements.stream()
                .filter(variableElement -> variableElement.getAnnotation(Limit.class) != null)
                .findFirst();
        Optional<? extends VariableElement> offsetVariableElement = variableElements.stream()
                .filter(variableElement -> variableElement.getAnnotation(OffSet.class) != null)
                .findFirst();

        if (limitVariableElement.isPresent() && offsetVariableElement.isPresent()) {
            sqlElement.addText(" limit #{" + offsetVariableElement.get().getSimpleName().toString() +
                    "}, #{" + limitVariableElement.get().getSimpleName().toString() + "}");
        } else if (limitVariableElement.isPresent()) {
            sqlElement.addText(" limit #{" + limitVariableElement.get().getSimpleName().toString() + "}");
        }
    }
}