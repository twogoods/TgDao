package com.tg.generator.sql.where.param;

import com.tg.constant.Constants;
import com.tg.util.StringUtils;
import org.dom4j.Element;

/**
 * Created by twogoods on 2017/8/5.
 */
public class SelectiveParamSqlGen implements WhereParamSqlGen {
    @Override
    public void generateWhereParamSql(Param param) {
        Element ifElement = param.getWhereElement().addElement("if");
        if (StringUtils.isEmpty(param.getTest())) {
            ifElement.addAttribute("test", param.getField() + " != null");
        } else {
            ifElement.addAttribute("test", param.getTest());
        }
        ifElement.addText(param.getAttach().name() + Constants.BLANK + param.getColumn() +
                Constants.BLANK + param.getCriterion().getCriterion() + " #{" + param.getField() + "} ");
    }
}
