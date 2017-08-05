package com.tg.generator.sql.where.param;

import com.tg.constant.Attach;
import com.tg.constant.Criterions;
import com.tg.util.StringUtils;
import org.dom4j.Element;

/**
 * Created by twogoods on 2017/8/5.
 */
public class DirectParamSqlGen implements WhereParamSqlGen {

    @Override
    public void generateWhereParamSql(Element whereElement, Criterions criterion, Attach attach, String column, String field) {
        whereElement.addText(attach.name() + StringUtils.BLANK + column
                + StringUtils.BLANK + criterion.getCriterion() + " #{" + field + "} ");
    }
}
