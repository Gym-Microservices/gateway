package com.gym.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.List;

@Configuration
public class GatewayConfig {
    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            return ReactiveSecurityContextHolder.getContext()
                    .cast(org.springframework.security.core.context.SecurityContext.class)
                    .map(securityContext -> securityContext.getAuthentication())
                    .cast(JwtAuthenticationToken.class)
                    .map(jwtAuth -> {
                        // Extract JWT information
                        Jwt jwt = jwtAuth.getToken();
                        String username = jwt.getClaimAsString("preferred_username");
                        String email = jwt.getClaimAsString("email");
                        String userId = jwt.getClaimAsString("sub");
                        List<String> roles = extractRoles(jwt);

                        // Create request with JWT headers
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
                        // On error, continue without auth headers
                        return chain.filter(exchange);
                    });
        };
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        try {
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof java.util.Map) {
                Object roles = ((java.util.Map<String, Object>) realmAccess).get("roles");
                if (roles instanceof List) {
                    return (List<String>) roles;
                }
            }
        } catch (Exception e) {
            // Ignore and return empty list
        }
        return List.of();
    }
}
