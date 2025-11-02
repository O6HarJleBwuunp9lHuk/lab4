package org.example.notification.circuitbreaker;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class CircuitBreakerAspect {

    @Autowired
    private CircuitBreakerRegistry registry;

    @Around("@annotation(circuitBreakerAnnotation)")
    public Object protect(ProceedingJoinPoint pjp, CircuitBreaker circuitBreakerAnnotation) throws Throwable {
        CircuitBreakerImpl breaker = registry.getCircuitBreaker(circuitBreakerAnnotation.name());

        if (!breaker.allowRequest()) {
            return executeFallback(pjp, circuitBreakerAnnotation);
        }

        try {
            Object result = pjp.proceed();
            breaker.recordSuccess();
            return result;
        } catch (Throwable e) {
            if (!shouldIgnore(e, circuitBreakerAnnotation.ignoreExceptions())) {
                breaker.recordFailure();
            }
            throw e;
        }
    }

    private Object executeFallback(ProceedingJoinPoint pjp, CircuitBreaker circuitBreakerAnnotation) throws Throwable {
        String fallbackMethod = circuitBreakerAnnotation.fallbackMethod();
        if (!fallbackMethod.isEmpty()) {
            try {
                Object target = pjp.getTarget();
                MethodSignature signature = (MethodSignature) pjp.getSignature();
                Method originalMethod = signature.getMethod();
                Class<?>[] parameterTypes = originalMethod.getParameterTypes();
                Method fallback = target.getClass().getMethod(fallbackMethod, parameterTypes);
                return fallback.invoke(target, pjp.getArgs());
            } catch (NoSuchMethodException e) {
                try {
                    Method fallback = pjp.getTarget().getClass().getMethod(fallbackMethod);
                    return fallback.invoke(pjp.getTarget());
                } catch (NoSuchMethodException e2) {
                    throw new CircuitBreakerOpenException("Fallback method '" + fallbackMethod + "' not found", e);
                }
            } catch (Exception e) {
                throw new CircuitBreakerOpenException("Fallback method '" + fallbackMethod + "' failed", e);
            }
        }
        throw new CircuitBreakerOpenException("Circuit breaker is OPEN for " + circuitBreakerAnnotation.name());
    }

    private boolean shouldIgnore(Throwable e, Class<? extends Throwable>[] ignoreExceptions) {
        for (Class<? extends Throwable> ignore : ignoreExceptions) {
            if (ignore.isInstance(e)) {
                return true;
            }
        }
        return false;
    }
}
