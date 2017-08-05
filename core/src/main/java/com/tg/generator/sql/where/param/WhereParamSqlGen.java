package com.tg.generator.sql.where.param;

import com.tg.constant.Attach;
import com.tg.constant.Criterions;
import org.dom4j.Element;

/**
 * Created by twogoods on 2017/8/5.
 */
public interface WhereParamSqlGen {
    void generateWhereParamSql(Element whereElement, Criterions criterion, Attach attach, String column, String field);
}
