package com.tg.constant;

/**
 * Created by twogoods on 2017/7/30.
 */
public enum Criterions {
    EQUAL("="),
    NOT_EQUAL("!="),
    GREATER(">"),
    GREATER_OR_EQUAL(">="),
    LESS("<="),
    LESS_OR_EQUAL("<="),
    IN("in"),
    NOT_IN("not in");


    private String criterion;

    Criterions(String criterion) {
        this.criterion = criterion;
    }

    public String getCriterion() {
        return criterion;
    }
}
