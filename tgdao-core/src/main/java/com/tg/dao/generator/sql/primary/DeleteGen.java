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
            checkOneParam();
            whereSqlGen = new ModelWhereSqlGen(executableElement, tableInfo, delete.sqlMode(), modelConditions);
            return;
        }
        whereSqlGen = new FlatParamWhereSqlGen(executableElement, tableInfo, delete.sqlMode());
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element deleteElement = root.addElement("delete");
        deleteElement.addAttribute("id", executableElement.getSimpleName().toString());
        deleteElement.addText("delete from " + tableInfo.getTableName());
        return deleteElement;
    }
}
