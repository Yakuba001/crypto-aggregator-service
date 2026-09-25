package com.yakuba.config;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;

@Configuration
public class ResilienceConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        IntervalFunction intervalWithExponentialBackoffAndJitter =
                IntervalFunction.ofExponentialRandomBackoff(100, 2.0);

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(intervalWithExponentialBackoffAndJitter)
                .retryExceptions(IOException.class, HttpServerErrorException.class)
                .ignoreExceptions(HttpClientErrorException.class)
                .build();

        return RetryRegistry.of(config);
    }

    @Bean
    public Retry defaultRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry("default");
    }
}
