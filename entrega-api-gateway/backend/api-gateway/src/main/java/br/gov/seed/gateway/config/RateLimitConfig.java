package br.gov.seed.gateway.config;

import java.util.Objects;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(20, 40);
    }

    @Bean
    public KeyResolver remoteAddressKeyResolver() {
        return exchange -> Mono.just(
            Objects.requireNonNullElse(
                exchange.getRequest().getRemoteAddress(),
                exchange.getRequest().getLocalAddress()
            ).getAddress().getHostAddress()
        );
    }
}
