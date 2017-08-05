package com.tg.generator.sql.where;

import com.tg.annotation.Condition;
import com.tg.annotation.ModelCondition;
import com.tg.constant.SqlMode;
import com.tg.generator.model.TableMapping;
import com.tg.generator.sql.AbstractSqlGen;
import com.tg.generator.sql.where.param.DirectParamSqlGen;
import com.tg.generator.sql.where.param.SelectiveParamSqlGen;
import com.tg.generator.sql.where.param.WhereParamSqlGen;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by twogoods on 2017/8/2.
 */
public abstract class AbstractWhereSqlGen extends AbstractSqlGen implements WhereSqlGen {
    protected WhereParamSqlGen whereParamSqlGen;

    public AbstractWhereSqlGen(ExecutableElement executableElement, TableMapping tableInfo, SqlMode sqlMode) {
        super(executableElement, tableInfo);
        if (sqlMode == SqlMode.SELECTIVE) {
            whereParamSqlGen = new SelectiveParamSqlGen();
        } else {
            whereParamSqlGen = new DirectParamSqlGen();
        }
    }

    protected void generateINSuffix(Condition condition, Element sqlElement, VariableElement variableElement) {
        Element ifElement = sqlElement.addElement("if");
        String param = "collection";//mybatis 不加@Param注解,数组列表的参数只能是collection,list,array这几种
        if (variableElement.asType().toString().contains("[]")) {
            ifElement.addAttribute("test", "array !=null and array.length > 0");
            param = "array";
        } else {
            ifElement.addAttribute("test", "collection !=null and collection.size() > 0");
        }
        String varName = variableElement.getSimpleName().toString();
        String column = StringUtils.isEmpty(condition.column()) ? varName : condition.column();
        ifElement.addText(condition.attach().name() + StringUtils.BLANK + column + StringUtils.BLANK + condition.value().getCriterion());
        generateForEach(ifElement, param);
    }

    protected void generateINSuffix(ModelCondition condition, Element sqlElement) {
        Element ifElement = sqlElement.addElement("if");
        String column = StringUtils.isEmpty(condition.column()) ? condition.field() : condition.column();
        ifElement.addAttribute("test", condition.field() + " !=null and " + condition.field() + ".size() > 0");
        ifElement.addText(condition.attach().name() + StringUtils.BLANK + column + StringUtils.BLANK + condition.criterion().getCriterion());
        generateForEach(ifElement, condition.field());
    }

    private void generateForEach(Element ifElement, String param) {
        Element each = ifElement.addElement("foreach");
        each.addAttribute("item", "item");
        each.addAttribute("collection", param);
        each.addAttribute("open", "(");
        each.addAttribute("separator", ",");
        each.addAttribute("close", ")");
        each.addText("#{item}");
    }
}
