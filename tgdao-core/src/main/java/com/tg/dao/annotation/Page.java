package com.tg.dao.annotation;

/**
 * Created by twogoods on 2017/7/28.
 */
public @interface Page {

    String offsetField() default "offset";

    String limitField() default "limit";

}
