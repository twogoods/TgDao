package com.tg.generator.sql.primary;

import com.tg.annotation.Delete;
import com.tg.annotation.ModelConditions;
import com.tg.constant.SqlMode;
import com.tg.exception.TgDaoException;
import com.tg.generator.model.TableMapping;
import com.tg.generator.sql.suffix.ModelSuffixGen;
import com.tg.generator.sql.suffix.ParamSuffixGen;
import com.tg.generator.sql.where.FlatParamWhereSqlGen;
import com.tg.generator.sql.where.ModelWhereSqlGen;
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
            setWhereSqlGen(new ModelWhereSqlGen(executableElement, tableInfo, SqlMode.COMMON, modelConditions));
            return;
        }
        setWhereSqlGen(new FlatParamWhereSqlGen(executableElement, tableInfo, SqlMode.COMMON));
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element deleteElement = root.addElement("delete");
        deleteElement.addAttribute("id", executableElement.getSimpleName().toString());
        deleteElement.addText("delete from " + tableInfo.getTableName());
        return deleteElement;
    }
}
