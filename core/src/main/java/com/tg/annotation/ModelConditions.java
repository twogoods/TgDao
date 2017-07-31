package com.tg.annotation;

import com.tg.constant.Attach;
import com.tg.constant.Criterions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by twogoods on 2017/7/31.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface ModelConditions {
    ModelCondition[] value() default {};
}
