package com.tg.dao.generator.sql.primary;

import com.tg.dao.generator.sql.where.FlatParamWhereSqlGen;
import com.tg.dao.generator.sql.where.ModelWhereSqlGen;
import com.tg.dao.annotation.Delete;
import com.tg.dao.annotation.ModelConditions;
import com.tg.dao.constant.SqlMode;
import com.tg.dao.exception.TgDaoException;
import com.tg.dao.generator.model.TableMapping;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/1.
 */
public class DeleteGen extends PrimarySqlGen {
    private Delete delete;

    public DeleteGen(ExecutableElement executableElement, TableMapping tableInfo, Delete delete) {
        super(executableElement, tableInfo);
        this.delete = delete;
    }

    @Override
    protected void checkAnnotatedRule() {
        ModelConditions modelConditions = executableElement.getAnnotation(ModelConditions.class);
        if (modelConditions != null) {
            if (executableElement.getParameters().size() != 1) {
                throw new TgDaoException(String.format("check method %s , support only one parameter", executableElement.getSimpleName().toString()));
            }
            whereSqlGen = new ModelWhereSqlGen(executableElement, tableInfo, SqlMode.COMMON, modelConditions);
            return;
        }
        whereSqlGen = new FlatParamWhereSqlGen(executableElement, tableInfo, SqlMode.COMMON);
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element deleteElement = root.addElement("delete");
        deleteElement.addAttribute("id", executableElement.getSimpleName().toString());
        deleteElement.addText("delete from " + tableInfo.getTableName());
        return deleteElement;
    }
}
