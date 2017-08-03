package com.tg.generator.sql.suffix;

import com.tg.annotation.OrderBy;
import com.tg.generator.model.TableMapping;
import com.tg.generator.sql.AbstractSqlGen;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/2.
 */
public abstract class AbstractSuffixSqlGen extends AbstractSqlGen implements SuffixSqlGen {
    public AbstractSuffixSqlGen(ExecutableElement executableElement, TableMapping tableInfo) {
        super(executableElement, tableInfo);
    }

    @Override
    public void generateSuffixSql(Element sqlElement) {
        generateOrder(sqlElement);
        generatePage(sqlElement);
    }

    protected abstract void generatePage(Element sqlElement);

    protected void generateOrder(Element sqlElement) {
        OrderBy orderBy = executableElement.getAnnotation(OrderBy.class);
        if (orderBy != null) {
            sqlElement.addText(" order by " + orderBy.value());
        }
    }
}
