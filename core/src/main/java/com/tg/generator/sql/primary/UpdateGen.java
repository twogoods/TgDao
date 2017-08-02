package com.tg.generator.sql.primary;

import com.tg.annotation.Update;
import com.tg.exception.TgDaoException;
import com.tg.generator.model.TableMapping;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/1.
 */
public class UpdateGen extends PrimarySqlGen {
    private Update update;

    public UpdateGen(ExecutableElement executableElement, TableMapping tableInfo, Update update) {
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
        //TODO
        Element setElement = updateElement.addElement("set");
    }
}
