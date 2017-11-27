package com.tg.dao.annotation;

import com.tg.dao.constant.SqlMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by twogoods on 2017/7/28.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Delete {
    SqlMode sqlMode() default SqlMode.COMMON;
}
