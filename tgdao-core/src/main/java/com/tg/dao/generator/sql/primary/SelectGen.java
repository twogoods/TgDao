package com.tg.dao.generator.sql.primary;

import com.tg.dao.generator.sql.where.ModelWhereSqlGen;
import com.tg.dao.annotation.ModelConditions;
import com.tg.dao.annotation.Select;
import com.tg.dao.constant.Constants;
import com.tg.dao.exception.TgDaoException;
import com.tg.dao.generator.model.TableMapping;
import com.tg.dao.generator.sql.suffix.ModelSuffixGen;
import com.tg.dao.generator.sql.suffix.ParamSuffixGen;
import com.tg.dao.generator.sql.where.FlatParamWhereSqlGen;
import com.tg.dao.util.StringUtils;
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
            checkOneParam();
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
