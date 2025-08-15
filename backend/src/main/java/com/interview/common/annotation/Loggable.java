package com.interview.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
    /**
     * Log method parameters
     */
    boolean logParams() default true;

    /**
     * Log return value
     */
    boolean logResult() default true;

    /**
     * Log execution time
     */
    boolean logExecutionTime() default true;

    /**
     * Log level for normal execution
     */
    String level() default "INFO";
}

