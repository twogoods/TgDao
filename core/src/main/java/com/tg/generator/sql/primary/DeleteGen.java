package com.tg.generator.sql.primary;

import com.tg.annotation.Delete;
import com.tg.generator.model.TableMapping;
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

    }

    @Override
    protected Element generateBaseSql(Element root) {
        Element deleteElement = root.addElement("delete");
        deleteElement.addAttribute("id", executableElement.getSimpleName().toString());
        deleteElement.addText("delete from " + tableInfo.getTableName());
        return deleteElement;
    }
}
