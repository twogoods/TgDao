package com.tg.dao.annotation;

import com.tg.dao.constant.Attach;
import com.tg.dao.constant.Criterions;
import com.tg.dao.constant.InType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by twogoods on 2017/7/31.
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface ModelCondition {
    Criterions criterion() default Criterions.EQUAL;

    String field();

    String column() default "";

    Attach attach() default Attach.AND;

    String test() default "";

    //in 查询时设置
    InType paramType() default InType.COLLECTION;
}
