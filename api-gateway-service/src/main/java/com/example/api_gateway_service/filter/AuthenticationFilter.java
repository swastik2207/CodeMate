package com.example.api_gateway_service.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;
import com.example.api_gateway_service.utils.JwtUtil;
import com.example.api_gateway_service.filter.RouteValidator;

@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (validator.isSecured.test(request)) {
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new RuntimeException("Missing auth header");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                throw new RuntimeException("Invalid Authorization header");
            }

            jwtUtil.validateToken(authHeader); // Throws if invalid

            String userId = jwtUtil.extractUserId(authHeader);
            request = request.mutate()
                             .header("x-user-id", userId)
                             .build();
            exchange = exchange.mutate().request(request).build();
        }

        return chain.filter(exchange);
    }
}
