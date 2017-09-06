package com.tg.dao.generator.sql.where;

import org.dom4j.Element;

/**
 * Created by twogoods on 2017/8/2.
 */
public interface WhereSqlGen {
    void generateWhereSql(Element sqlElement);
}
