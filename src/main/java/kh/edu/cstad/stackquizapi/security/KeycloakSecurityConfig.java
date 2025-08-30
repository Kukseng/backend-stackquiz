package kh.edu.cstad.stackquizapi.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Security configuration for integrating Keycloak with Spring Security.
 * <p>
 * Configures JWT authentication, role mapping, and HTTP security rules
 * for the API. Ensures stateless session management and disables form login/CSRF
 * for API-based authentication.
 * </p>
 *
 * @author PECH RATTANAKMONY
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class KeycloakSecurityConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        Converter<Jwt, Collection<GrantedAuthority>> converter = jwt -> {
            try {
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                if (realmAccess == null || !realmAccess.containsKey("roles")) {
                    log.debug("No realm_access roles found in JWT token");
                    return Collections.emptyList();
                }

                @SuppressWarnings("unchecked")
                Collection<String> roles = (Collection<String>) realmAccess.get("roles");
                if (roles == null) {
                    return Collections.emptyList();
                }

                return roles.stream()
                        .filter(Objects::nonNull)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

            } catch (Exception e) {
                log.warn("Error extracting roles from JWT: {}", e.getMessage());
                return Collections.emptyList();
            }
        };

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/v1/auth/**").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/v1/questions/**").hasRole("ORGANIZER")
//                        .requestMatchers(HttpMethod.GET, "/api/v1/questions/**").hasRole("ORGANIZER")
//                        .requestMatchers(HttpMethod.PATCH, "/api/v1/questions/**").hasRole("ORGANIZER")
//                        .requestMatchers(HttpMethod.DELETE, "/api/v1/questions/**").hasRole("ORGANIZER")
//                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

}
