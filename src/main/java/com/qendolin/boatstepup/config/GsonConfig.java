package com.qendolin.boatstepup.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GsonConfig {
    /**
     * The name of the config file.
     * It's recommended to use the mod id.
     */
    String value();

    int version() default 1;
}