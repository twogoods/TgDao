package com.tg.generator.sql;

import com.tg.annotation.Delete;
import com.tg.generator.model.TableMapping;
import org.dom4j.Element;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by twogoods on 2017/8/1.
 */
public class DeleteSql extends SqlGen {
    private Delete delete;

    public DeleteSql(ExecutableElement executableElement, TableMapping tableInfo, Delete delete) {
        super(executableElement, tableInfo);
        this.delete = delete;
    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element deleteElement = root.addElement("delete");
        deleteElement.addAttribute("id", executableElement.getSimpleName().toString());
        deleteElement.addText("delete from " + tableInfo.getTableName());
        return deleteElement;
    }

    @Override
    protected void generateOrderAndPage(Element sqlElement) {
        commonWhereSql(sqlElement);
    }

    @Override
    protected void generateWhereSql(Element sqlElement) {

    }
}
