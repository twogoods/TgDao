package com.tg.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 *
 * @author twogoods
 * @version 0.1
 * @since 2017-05-06
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface DaoGen {
    Class<?> model();
}