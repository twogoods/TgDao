package com.tg.dao.util;

/**
 * Created by twogoods on 2017/7/28.
 */
public class StringUtils {
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNotEmpty(String s) {
        return s != null && s.length() > 0;
    }
}
