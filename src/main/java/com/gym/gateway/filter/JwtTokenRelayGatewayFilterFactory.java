package com.gym.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class JwtTokenRelayGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            return ReactiveSecurityContextHolder.getContext()
                    .cast(org.springframework.security.core.context.SecurityContext.class)
                    .map(securityContext -> securityContext.getAuthentication())
                    .cast(JwtAuthenticationToken.class)
                    .map(jwtAuth -> {
                        Jwt jwt = jwtAuth.getToken();

                        // Extract JWT information
                        String username = jwt.getClaimAsString("preferred_username");
                        String email = jwt.getClaimAsString("email");
                        String userId = jwt.getClaimAsString("sub");

                        // Extract roles from realm_access
                        List<String> roles = extractRoles(jwt);

                        // Create new request with additional headers
                        ServerHttpRequest request = exchange.getRequest().mutate()
                                .header("X-User-Id", userId != null ? userId : "")
                                .header("X-User-Name", username != null ? username : "")
                                .header("X-User-Email", email != null ? email : "")
                                .header("X-User-Roles", String.join(",", roles))
                                .header("X-Auth-Source", "gateway")
                                .build();

                        return exchange.mutate().request(request).build();
                    })
                    .defaultIfEmpty(exchange) // If no authentication, use original exchange
                    .flatMap(modifiedExchange -> chain.filter(modifiedExchange))
                    .onErrorResume(throwable -> {
                        // On error, continue without authentication headers
                        System.err.println("Error processing JWT in Gateway: " + throwable.getMessage());
                        return chain.filter(exchange);
                    });
        };
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                return (List<String>) realmAccess.get("roles");
            }
        } catch (Exception e) {
            // Log error but continue
            System.err.println("Error extracting roles from JWT: " + e.getMessage());
        }
        return List.of(); // Return empty list if no roles
    }
}
