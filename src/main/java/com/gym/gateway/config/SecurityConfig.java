package com.gym.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.core.GrantedAuthority;
import reactor.core.publisher.Flux;
import java.util.Collection;

import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/swagger/**", "/api-docs/**").permitAll()
                        .pathMatchers("/api/*/actuator/health/**").permitAll()
                        .pathMatchers("/api/health").permitAll()
                        .pathMatchers("/api/test").permitAll()
                        .pathMatchers("/api/routes").permitAll()
                        .pathMatchers("/api/aggregation/members/*/summary").authenticated()
                        .pathMatchers("/api/**", "/*/api/**",
                                "/class-microservice/api/**", "/member-microservice/api/**",
                                "/coach-microservice/api/**", "/equipment-microservice/api/**",
                                "/payment-microservice/api/**", "/notification-microservice/api/**")
                        .authenticated()
                        .anyExchange().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);
            return Flux.fromIterable(authorities);
        });

        return jwtAuthenticationConverter;
    }
}
