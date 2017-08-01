package com.tg.generator.sql;

import com.tg.annotation.Update;
import com.tg.exception.TgDaoException;
import com.tg.generator.model.TableMapping;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/1.
 */
public class UpdateSql extends SqlGen {
    private Update update;

    public UpdateSql(ExecutableElement executableElement, TableMapping tableInfo, Update update) {
        super(executableElement, tableInfo);
        this.update = update;
    }

    @Override
    protected void checkAnnotatedRule() {
        if (executableElement.getParameters().size() != 1) {
            throw new TgDaoException(String.format("check method %s , support only one parameter", executableElement.getSimpleName().toString()));
        }
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element updateElement = root.addElement("update");
        updateElement.addAttribute("id", executableElement.getSimpleName().toString());
        updateElement.addText("update " + tableInfo.getTableName());
        return updateElement;
    }
    
    private void generateSet(Element updateElement) {
        Element setElement = updateElement.addElement("set");
    }

    @Override
    protected void generateOrderAndPage(Element sqlElement) {

    }

    @Override
    protected void generateWhereSql(Element sqlElement) {

    }
}
