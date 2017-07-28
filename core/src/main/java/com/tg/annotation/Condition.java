package com.tg.annotation;

import com.tg.constant.Attach;

/**
 * Created by twogoods on 2017/7/28.
 */
public @interface Condition {
    String value() default "";

    String column() default "";

    Attach attach() default Attach.AND;


}
