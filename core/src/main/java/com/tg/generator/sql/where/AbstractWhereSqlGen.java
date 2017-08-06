package com.tg.generator.sql.where;

import com.tg.annotation.Condition;
import com.tg.annotation.ModelCondition;
import com.tg.constant.Attach;
import com.tg.constant.Criterions;
import com.tg.constant.InType;
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
        String varName = variableElement.getSimpleName().toString();
        String column = StringUtils.isEmpty(condition.column()) ? varName : condition.column();
        InType inType = variableElement.asType().toString().contains("[]") ? InType.ARRAY : InType.COLLECTION;
        if (executableElement.getParameters().size() == 1) {
            /*
             * mybatis  可以通过编译时加-parameters 去掉@Param注解
             * 但是在方法只有一个参数,并且是数组或者list时,mybatis 似乎不会把方法的参数名当做可以获取值得那个名字
             * 那个名字是collection,list,array这几种
             */
            inSuffix(sqlElement, condition.attach(), condition.value(), column, inType.getName(), inType);
            return;
        }
        inSuffix(sqlElement, condition.attach(), condition.value(), column, varName, inType);
    }

    protected void generateINSuffix(ModelCondition condition, Element sqlElement) {
        String column = StringUtils.isEmpty(condition.column()) ? condition.field() : condition.column();
        inSuffix(sqlElement, condition.attach(), condition.criterion(), column, condition.field(), condition.paramType());
    }

    private void inSuffix(Element sqlElement, Attach attach, Criterions criterion, String column, String field, InType inType) {
        Element ifElement = sqlElement.addElement("if");
        ifElement.addAttribute("test", field + " !=null and " + field + "." + inType.getCheckExpress() + " > 0");
        ifElement.addText(attach.name() + StringUtils.BLANK + column + StringUtils.BLANK + criterion.getCriterion());
        generateForEach(ifElement, field);
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
