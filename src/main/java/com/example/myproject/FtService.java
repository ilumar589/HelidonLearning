package com.example.myproject;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.faulttolerance.Async;
import io.helidon.faulttolerance.Bulkhead;
import io.helidon.faulttolerance.CircuitBreaker;
import io.helidon.faulttolerance.Fallback;
import io.helidon.faulttolerance.FallbackConfig;
import io.helidon.faulttolerance.Retry;
import io.helidon.faulttolerance.Timeout;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class FtService implements HttpService {

    private final Bulkhead bulkhead;
    private final Async async;
    private final CircuitBreaker circuitBreaker;
    private final Fallback<String> fallback;
    private final Retry retry;
    private final Timeout timeout;

    FtService() {
        this.async = Async.create();
        this.fallback = Fallback.create(FallbackConfig.<String>builder().fallback(this::fallback).build());
        this.bulkhead = Bulkhead.builder()
                .queueLength(1)
                .limit(1)
                .name("helidon-example-bulkhead")
                .build();
        this.circuitBreaker = CircuitBreaker.builder()
                .volume(4)
                .errorRatio(40)
                .successThreshold(1)
                .delay(Duration.ofSeconds(5))
                .build();
        this.retry = Retry.builder()
                .retryPolicy(Retry.DelayingRetryPolicy.noDelay(3))
                .build();
        this.timeout = Timeout.create(Duration.ofMillis(100));
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.get("/async", this::asyncHandler)
                .get("/bulkhead/{millis}", this::bulkheadHandler)
                .get("/circuitBreaker/{success}", this::circuitBreakerHandler)
                .get("/fallback/{success}", this::fallbackHandler)
                .get("/retry/{count}", this::retryHandler)
                .get("/timeout/{millis}", this::timeoutHandler);
    }

    private void timeoutHandler(ServerRequest request, ServerResponse response) {
        long sleep = request.path().pathParameters().first("millis").getLong();

        response.send(timeout.invoke(() -> sleep(sleep)));
    }

    private void retryHandler(ServerRequest request, ServerResponse response) {
        int count = request.path().pathParameters().first("count").getInt();

        AtomicInteger call = new AtomicInteger(1);
        AtomicInteger failures = new AtomicInteger();

        response.send(retry.invoke(() -> {
            int current = call.getAndIncrement();
            if (current < count) {
                failures.incrementAndGet();
                return reactiveFailure();
            }
            return "calls/failures: " + current + "/" + failures.get();
        }));
    }

    private void fallbackHandler(ServerRequest request, ServerResponse response) {
        boolean success = request.path().pathParameters().first("success").getBoolean();
        if (success) {
            response.send(fallback.invoke(this::reactiveData));
        } else {
            response.send(fallback.invoke(this::reactiveFailure));
        }
    }

    private void circuitBreakerHandler(ServerRequest request, ServerResponse response) {
        boolean success = request.path().pathParameters().first("success").getBoolean();
        if (success) {
            response.send(circuitBreaker.invoke(this::reactiveData));
        } else {
            response.send(circuitBreaker.invoke(this::reactiveFailure));
        }
    }

    private void bulkheadHandler(ServerRequest request, ServerResponse response) {
        long sleep = request.path().pathParameters().first("millis").getLong();
        CompletableFuture<String> future = async.invoke(() -> bulkhead.invoke(() -> sleep(sleep)));
        sleep(100);
        if (bulkhead.stats().callsRejected() > 0) {
            try {
                response.send(future.get());
                return;
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
        response.send("Call rejected: " + bulkhead.stats().callsRejected());
    }

    private void asyncHandler(ServerRequest request, ServerResponse response) {
        response.send(reactiveData());
    }

    private String reactiveFailure() {
        throw new RuntimeException("reactive failure");
    }

    private String sleep(long sleepMillis) {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ignored) {
        }
        return "Slept for " + sleepMillis + " ms";
    }

    private String reactiveData() {
        try {
            return async.invoke(this::blockingData).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private String fallback(Throwable throwable) {
        return "Failed back because of " + throwable.getMessage();
    }

    private String blockingData() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        return "blocked for 100 millis";
    }

}
