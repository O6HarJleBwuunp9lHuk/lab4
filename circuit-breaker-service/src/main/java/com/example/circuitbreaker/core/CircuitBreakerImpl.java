package com.example.circuitbreaker.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class CircuitBreakerImpl {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerImpl.class);
    private final String name;
    private final int failureThreshold;
    private final long timeoutMs;
    private final AtomicInteger failures = new AtomicInteger(0);
    private final AtomicInteger successes = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private volatile State state = State.CLOSED;
    private final ReentrantLock lock = new ReentrantLock();

    public CircuitBreakerImpl(String name, int failureThreshold, long timeoutMs) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.timeoutMs = timeoutMs;
        logger.debug("CircuitBreaker initialized: name={}, failureThreshold={}, timeoutMs={}",
            name, failureThreshold, timeoutMs);
    }

    public boolean allowRequest() {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime.get() > timeoutMs) {
                lock.lock();
                try {
                    if (state == State.OPEN) {
                        state = State.HALF_OPEN;
                        successes.set(0);
                        logger.info("Circuit breaker {} moved to HALF_OPEN state", name);
                    }
                } finally {
                    lock.unlock();
                }
                return true;
            }
            logger.debug("Circuit breaker {} is OPEN, request blocked", name);
            return false;
        }
        return true;
    }

    public void recordSuccess() {
        lock.lock();
        try {
            if (state == State.HALF_OPEN) {
                successes.incrementAndGet();
                logger.debug("Circuit breaker {} recorded success in HALF_OPEN state: {}/{}",
                    name, successes.get(), failureThreshold);
                if (successes.get() >= failureThreshold) {
                    state = State.CLOSED;
                    failures.set(0);
                    logger.info("Circuit breaker {} moved to CLOSED state after {} successful requests",
                        name, successes.get());
                }
            } else {
                failures.set(0);
                logger.debug("Circuit breaker {} reset failure count on success", name);
            }
        } finally {
            lock.unlock();
        }
    }

    public void recordFailure() {
        lock.lock();
        try {
            failures.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());
            logger.debug("Circuit breaker {} recorded failure: {}/{}",
                name, failures.get(), failureThreshold);

            if (state == State.HALF_OPEN || failures.get() >= failureThreshold) {
                state = State.OPEN;
                logger.warn("Circuit breaker {} moved to OPEN state. Total failures: {}",
                    name, failures.get());
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isOpen() {
        return state == State.OPEN;
    }

    public State getState() {
        return state;
    }

    public int getFailureCount() {
        return failures.get();
    }

    public int getSuccessCount() {
        return successes.get();
    }

    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }
}
