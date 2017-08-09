package com.tg.generator.sql.where.param;

import com.tg.constant.Constants;

/**
 * Created by twogoods on 2017/8/5.
 */
public class DirectParamSqlGen implements WhereParamSqlGen {
    @Override
    public void generateWhereParamSql(Param param) {
        param.getWhereElement().addText(param.getAttach().name() + Constants.BLANK + param.getColumn()
                + Constants.BLANK + param.getCriterion().getCriterion() + " #{" + param.getField() + "} ");
    }
}
