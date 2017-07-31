package com.tg.generator.sql;

import com.tg.annotation.Condition;
import com.tg.annotation.OffSet;
import com.tg.annotation.Limit;
import com.tg.generator.model.TableMapping;
import com.tg.constant.Attach;
import com.tg.constant.Criterions;
import com.tg.util.StringUtils;
import org.dom4j.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

/**
 * Created by twogoods on 2017/7/31.
 */
public abstract class SqlGen {
    protected ExecutableElement executableElement;
    protected Element root;
    protected TableMapping tableInfo;
    protected List<? extends VariableElement> variableElements;

    public SqlGen(ExecutableElement executableElement, Element root, TableMapping tableInfo) {
        this.executableElement = executableElement;
        this.root = root;
        this.tableInfo = tableInfo;
        variableElements = executableElement.getParameters();
    }

    public void generateSql(Element root) {
        Element sqlElement = generateBaseSql(root);
        generateWhereSql(sqlElement);
        generateOrderAndPage(sqlElement);
    }

    protected abstract Element generateBaseSql(Element root);

    protected abstract void generateWhereSql(Element sqlElement);

    protected abstract void generateOrderAndPage(Element sqlElement);

    protected void generateWhereParams(VariableElement variableElement, Element whereElement, int index) {
        if (isPageParam(variableElement)) {
            return;
        }
        String varName = variableElement.getSimpleName().toString();
        Condition condition = variableElement.getAnnotation(Condition.class);
        StringBuilder sqlBuilder = new StringBuilder();
        if (condition == null) {
            sqlBuilder.append(Attach.AND.name())
                    .append(StringUtils.BLANK)
                    .append(getColumn(condition, varName))
                    .append(" = ")
                    .append("#{").append(index).append("} ");
            return;
        }
        Criterions criterion = condition.value();
        if (criterion == Criterions.IN || criterion == Criterions.NOT_IN) {
            generateINSuffix(criterion, whereElement, StringUtils.isEmpty(condition.column()) ? varName : condition.column(), varName);
        } else {
            sqlBuilder.append(condition.attach().name())
                    .append(StringUtils.BLANK)
                    .append(getColumn(condition, varName))
                    .append(StringUtils.BLANK)
                    .append(criterion.getCriterion())
                    .append(" #{").append(index).append("} ");
            whereElement.addText(sqlBuilder.toString());
        }
    }

    protected String getColumn(Condition condition, String varName) {
        String column = null;
        if (condition == null) {
            column = tableInfo.getFieldToColumn().get(varName);
            return StringUtils.isEmpty(column) ? varName : column;
        }
        return StringUtils.isEmpty(condition.column()) ? varName : condition.column();
    }

    protected void generateWhereParamsSelective(VariableElement variableElement, Element whereElement, int index) {
        //TODO 解决mybatis @Param() 的问题
    }

    private void generateINSuffix(Criterions criterion, Element sqlElement, String column, String varName) {
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

    private void generateFirstParam(VariableElement variableElement, StringBuilder sqlBuilder) {
        String varName = variableElement.getSimpleName().toString();
        Condition condition = variableElement.getAnnotation(Condition.class);
        if (condition == null) {
            sqlBuilder.append(varName).append(" = ").append("#{").append(0).append("} ");
            return;
        }
        Attach attach = condition.attach();
        String column = condition.column();
        Criterions criterion = condition.value();
        sqlBuilder.append(attach.name())
                .append(StringUtils.BLANK)
                .append(StringUtils.isEmpty(column) ? varName : column)
                .append(StringUtils.BLANK);
        if (criterion == Criterions.IN || criterion == Criterions.NOT_IN) {
            sqlBuilder.append(criterion.getCriterion());
        } else {
            sqlBuilder.append(criterion.getCriterion()).append(" #{").append(0).append("} ");
        }
    }

    protected boolean isPageParam(VariableElement variableElement) {
        boolean flag = variableElement.getAnnotation(Limit.class) != null || variableElement.getAnnotation(OffSet.class) != null;
        return flag;
    }

    public Element generateTrimElement(Element element) {
        Element trimElement = element.addElement("trim")
                .addAttribute("prefix", "(")
                .addAttribute("suffix", ")")
                .addAttribute("suffixOverrides", ",");
        return trimElement;
    }
}
