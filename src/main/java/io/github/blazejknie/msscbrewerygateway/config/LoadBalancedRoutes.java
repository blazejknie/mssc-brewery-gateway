package io.github.blazejknie.msscbrewerygateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("local_discovery")
@Configuration
public class LoadBalancedRoutes {

    @Bean
    public RouteLocator loadBalanced(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("beer-service", r -> r.path("/api/v1/beer*", "/api/v1/beer/*", "/api/v1/beerUpc/*")
                        .uri("lb://beer-service"))
                .route("beer-order-service",
                        r -> r.path("/api/v1/customers/*/orders*", "/api/v1/customers/*/orders/*",
                                "/api/v1/customers/*/orders/*/pickup", "/api/v1/customers*",
                                "/api/v1/customers/*").uri("lb://beer-order-service"))
                .route("inventory-service",
                        r -> r.path("/api/v1/beer/*/inventory")
                                .filters(f -> f.circuitBreaker(c -> c.setName("inventoryCB")
                                        .setFallbackUri("forward:/inventory-failover")
                                        .setRouteId("inv-failover")))
                                .uri("lb://beer-inventory-service"))
                .route("inventory-failover", r -> r.path("/inventory-failover/**")
                        .uri("lb://beer-inventory-failover-service"))
                .build();
    }
}
