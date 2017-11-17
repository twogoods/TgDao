package com.tg.dao.generator.sql.where;

import com.tg.dao.annotation.ModelCondition;
import com.tg.dao.constant.*;
import com.tg.dao.generator.sql.where.param.WhereParamSqlGen;
import com.tg.dao.annotation.Condition;
import com.tg.dao.generator.model.TableMapping;
import com.tg.dao.generator.sql.AbstractSqlGen;
import com.tg.dao.generator.sql.where.param.DirectParamSqlGen;
import com.tg.dao.generator.sql.where.param.SelectiveParamSqlGen;
import com.tg.dao.util.StringUtils;
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
            //bugfix 方法直接传一个基本类型的查询条件(如string),即只有一个参数时不能是selective的,而一个类的对象是可以的
            /* 用户自己添加 @Param 解决，这里不处理
            if (executableElement.getAnnotation(ModelConditions.class) == null &&
                    executableElement.getParameters().size() == 1) {
                whereParamSqlGen = new DirectParamSqlGen();
                return;
            }
            */
            whereParamSqlGen = new SelectiveParamSqlGen();
        } else {
            whereParamSqlGen = new DirectParamSqlGen();
        }
    }

    protected void generateINSuffix(Condition condition, Element sqlElement, VariableElement variableElement) {
        String varName = variableElement.getSimpleName().toString();
        String column = StringUtils.isEmpty(condition.column()) ? varName : condition.column();
        InType inType = variableElement.asType().toString().contains("[]") ? InType.ARRAY : InType.COLLECTION;
        /*
         * mybatis  可以通过编译时加-parameters 去掉@Param注解
         * 但是在方法只有一个参数,并且是数组或者list时,mybatis 似乎不会把方法的参数名当做可以获取值的那个名字
         * 那个名字是collection,list,array这几种
         */
        if (commonNameForEach(variableElement)) {
            inSuffix(sqlElement, condition.attach(), condition.criterion(), column, inType.getName(), inType);
            return;
        }
        inSuffix(sqlElement, condition.attach(), condition.criterion(), column, varName, inType);
    }


    protected void generateINSuffix(ModelCondition condition, Element sqlElement, String field) {
        String column = StringUtils.isEmpty(condition.column()) ? condition.field() : condition.column();
        inSuffix(sqlElement, condition.attach(), condition.criterion(), column, field, condition.paramType());
    }

    private void inSuffix(Element sqlElement, Attach attach, Criterions criterion, String column, String field, InType inType) {
        Element ifElement = sqlElement.addElement("if");
        ifElement.addAttribute("test", field + " !=null and " + field + "." + inType.getCheckExpress() + " > 0");
        ifElement.addText(attach.name() + Constants.BLANK + column + Constants.BLANK + criterion.getCriterion());
        generateForEach(ifElement, field);
    }

    private void generateForEach(Element ifElement, String param) {
        Element each = ifElement.addElement("foreach");
        each.addAttribute("item", "item");
        each.addAttribute("collection", param);
        each.addAttribute("open", "(");
        each.addAttribute("separator", Constants.separator);
        each.addAttribute("close", ")");
        each.addText("#{item}");
    }
}
