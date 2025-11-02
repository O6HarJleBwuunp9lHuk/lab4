package org.example.notification.circuitbreaker;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CircuitBreaker {
    String name();

    int failureThreshold() default 5;

    long timeout() default 30000;

    Class<? extends Throwable>[] ignoreExceptions() default {};

    String fallbackMethod() default "";
}
