package com.tg.annotation;

import com.tg.constant.SqlMode;

/**
 * Created by twogoods on 2017/7/28.
 */
public @interface Insert {
    SqlMode sqlMode() default SqlMode.common;
}
