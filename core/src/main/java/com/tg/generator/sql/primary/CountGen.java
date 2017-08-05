package com.tg.generator.sql.primary;

import com.tg.annotation.Count;
import com.tg.annotation.ModelConditions;
import com.tg.exception.TgDaoException;
import com.tg.generator.model.TableMapping;
import com.tg.generator.sql.where.FlatParamWhereSqlGen;
import com.tg.generator.sql.where.ModelWhereSqlGen;
import com.tg.util.StringUtils;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by twogoods on 2017/7/31.
 */
public class CountGen extends PrimarySqlGen {
    private Count count;

    public CountGen(ExecutableElement executableElement, TableMapping tableInfo, Count count) {
        super(executableElement, tableInfo);
        this.count = count;
    }

    @Override
    protected void checkAnnotatedRule() {
        ModelConditions modelConditions = executableElement.getAnnotation(ModelConditions.class);
        if (modelConditions != null) {
            if (executableElement.getParameters().size() != 1) {
                throw new TgDaoException(String.format("check method %s , support only one parameter", executableElement.getSimpleName().toString()));
            }
            whereSqlGen = new ModelWhereSqlGen(executableElement, tableInfo, count.sqlMode(), modelConditions);
            return;
        }
        whereSqlGen = new FlatParamWhereSqlGen(executableElement, tableInfo, count.sqlMode());
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element selectElement = root.addElement("select");
        selectElement.addAttribute("id", executableElement.getSimpleName().toString());
        selectElement.addAttribute("resultType", executableElement.getReturnType().toString());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select count(*)").append(" from ");
        sqlBuilder.append(tableInfo.getTableName()).append(StringUtils.BLANK);
        selectElement.addText(sqlBuilder.toString());
        return selectElement;
    }
}
