package com.tg.dao.constant;

/**
 * Created by twogoods on 2017/8/6.
 */
public enum InType {
    ARRAY("array", "length"), COLLECTION("collection", "size()");

    private String name;
    private String checkExpress;

    InType(String name, String checkExpress) {
        this.name = name;
        this.checkExpress = checkExpress;
    }

    public String getName() {
        return name;
    }

    public String getCheckExpress() {
        return checkExpress;
    }
}
