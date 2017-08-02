package com.tg.generator.sql;

import org.dom4j.Element;

/**
 * Created by twogoods on 2017/8/2.
 */
public interface SqlGen {
    void generateSql(Element root);
}
