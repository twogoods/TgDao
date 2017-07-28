package com.tg.annotation;

import com.tg.constant.SqlMode;

/**
 * Created by twogoods on 2017/7/28.
 */
public @interface Select {
    String columns() default "";

    SqlMode sqlMode() default SqlMode.common;
}
