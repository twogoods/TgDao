package com.tg.dao.generator.sql.where.param;

import com.tg.dao.constant.Attach;
import com.tg.dao.constant.Criterions;
import org.dom4j.Element;

/**
 * Created by twogoods on 2017/8/9.
 */
public class Param {
    private Element whereElement;
    private Criterions criterion;
    private Attach attach;
    private String column;
    private String field;
    private String test;

    public Param(Element whereElement, Criterions criterion, Attach attach, String column, String field, String test) {
        this.whereElement = whereElement;
        this.criterion = criterion;
        this.attach = attach;
        this.column = column;
        this.field = field;
        this.test = test;
    }

    public static class Builder {
        private Element whereElement;
        private Criterions criterion;
        private Attach attach;
        private String column;
        private String field;
        private String test;

        public Builder whereElement(Element whereElement) {
            this.whereElement = whereElement;
            return this;
        }

        public Builder criterion(Criterions criterion) {
            this.criterion = criterion;
            return this;
        }

        public Builder attach(Attach attach) {
            this.attach = attach;
            return this;
        }

        public Builder column(String column) {
            this.column = column;
            return this;
        }

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder test(String test) {
            this.test = test;
            return this;
        }

        public Param build() {
            return new Param(whereElement, criterion, attach, column, field, test);
        }
    }

    public Element getWhereElement() {
        return whereElement;
    }

    public Criterions getCriterion() {
        return criterion;
    }

    public Attach getAttach() {
        return attach;
    }

    public String getColumn() {
        return column;
    }

    public String getField() {
        return field;
    }

    public String getTest() {
        return test;
    }
}
