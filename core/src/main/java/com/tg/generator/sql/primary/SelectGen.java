package com.tg.generator.sql.primary;

import com.tg.annotation.ModelConditions;
import com.tg.annotation.Select;
import com.tg.constant.Constants;
import com.tg.exception.TgDaoException;
import com.tg.generator.model.TableMapping;
import com.tg.generator.sql.suffix.ModelSuffixGen;
import com.tg.generator.sql.suffix.ParamSuffixGen;
import com.tg.generator.sql.where.FlatParamWhereSqlGen;
import com.tg.generator.sql.where.ModelWhereSqlGen;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/7/31.
 */
public class SelectGen extends PrimarySqlGen {
    private Select select;

    public SelectGen(ExecutableElement executableElement, TableMapping tableInfo, Select select) {
        super(executableElement, tableInfo);
        this.select = select;
    }

    @Override
    protected void checkAnnotatedRule() {
        ModelConditions modelConditions = executableElement.getAnnotation(ModelConditions.class);
        if (modelConditions != null) {
            if (executableElement.getParameters().size() != 1) {
                throw new TgDaoException(String.format("check method %s , support only one parameter", executableElement.getSimpleName().toString()));
            }
            whereSqlGen = new ModelWhereSqlGen(executableElement, tableInfo, select.sqlMode(), modelConditions);
            suffixSqlGen = new ModelSuffixGen(executableElement, tableInfo);
            return;
        }
        whereSqlGen = new FlatParamWhereSqlGen(executableElement, tableInfo, select.sqlMode());
        suffixSqlGen = new ParamSuffixGen(executableElement, tableInfo);
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element selectElement = root.addElement("select");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString());
        String returnType = executableElement.getReturnType().toString();
        if (returnType.contains(tableInfo.getClassName())) {
            selectElement.addAttribute("resultMap", Constants.RESULT_MAP);
        } else {
            selectElement.addAttribute("resultType", returnType);
        }
        StringBuilder sqlBuilder = new StringBuilder();
        String columns = select.columns();
        if (StringUtils.isEmpty(columns)) {
            sqlBuilder.append("select * from ");
        } else {
            sqlBuilder.append("select ").append(columns).append(" from ");
        }
        sqlBuilder.append(tableInfo.getTableName()).append(Constants.BLANK);
        selectElement.addText(sqlBuilder.toString());
        return selectElement;
    }
}
