package com.tg.generator.sql.where;

import com.tg.constant.Criterions;
import com.tg.constant.SqlMode;
import com.tg.generator.model.TableMapping;
import com.tg.generator.sql.AbstractSqlGen;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/2.
 */
public abstract class AbstractWhereSqlGen extends AbstractSqlGen implements WhereSqlGen {
    protected SqlMode sqlMode;

    public AbstractWhereSqlGen(ExecutableElement executableElement, TableMapping tableInfo, SqlMode sqlMode) {
        super(executableElement, tableInfo);
        this.sqlMode = sqlMode;
    }

    protected void generateINSuffix(Criterions criterion, Element sqlElement, String column, String varName) {
        Element when = sqlElement.addElement("if");
        when.addAttribute("test", varName + " !=null and " + varName + ".size() > 0");
        when.addText(column + StringUtils.BLANK + criterion.getCriterion());
        Element each = when.addElement("foreach");
        each.addAttribute("item", "item");
        each.addAttribute("collection", varName);
        each.addAttribute("open", "(");
        each.addAttribute("separator", ",");
        each.addAttribute("close", ")");
        each.addText("#{item}");
    }
}
