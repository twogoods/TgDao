package com.tg.annotation;

import com.tg.constant.Attach;
import com.tg.constant.Criterions;

/**
 * Created by twogoods on 2017/7/28.
 */
public @interface Condition {
    Criterions value() default Criterions.EQUAL;

    String column() default "";

    Attach attach() default Attach.AND;


}
