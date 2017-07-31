package com.tg.generator.model;

import com.tg.constant.Attach;
import com.tg.constant.Criterions;
import com.tg.util.StringUtils;

/**
 * Created by twogoods on 2017/7/31.
 */
public class Conditions {
    private Attach attach = Attach.AND;
    private String field;
    private Criterions criterion = Criterions.EQUAL;

    private Conditions() {
    }

    private Conditions(String field) {
        this.field = field;
    }

    private Conditions(String field, Criterions criterion) {
        this.field = field;
        this.criterion = criterion;
    }

    private Conditions(Attach attach, String field, Criterions criterion) {
        this.attach = attach;
        this.field = field;
        this.criterion = criterion;
    }

    public static Conditions of(String field) {
        return new Conditions(field);
    }

    public static Conditions of(String field, Criterions criterion) {
        return new Conditions(field, criterion);
    }

    public static Conditions of(Attach attach, String field, Criterions criterion) {
        return new Conditions(attach, field, criterion);
    }
}
