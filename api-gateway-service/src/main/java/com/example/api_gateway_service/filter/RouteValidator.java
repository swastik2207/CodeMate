package com.example.api_gateway_service.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    // List of endpoints that DO NOT require authentication
    public static final List<String> openApiEndpoints = List.of(
        "/auth/register",
        "/auth/login"
    );

    // Predicate that returns true if the request URI is NOT in the openApiEndpoints
    public Predicate<ServerHttpRequest> isSecured =
        request -> openApiEndpoints.stream()
            .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
