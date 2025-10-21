package kh.edu.cstad.stackquizapi.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
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

                        // Swagger & health endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/health",
                                "/media/**"
                        ).permitAll()

                        // Websocket
                        .requestMatchers("/ws/**").permitAll()

                        // Auth endpoints
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/request-reset-password").hasRole("ORGANIZER")
                        .requestMatchers("/api/v1/auth/reset-password").hasRole("ADMIN")

                        // Participant endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/participants/session/*/can-join").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/participants/session/*/nickname-available").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/participants/join").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/participants/submit-answer").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/participants/session/*/can-join").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/participants/session/*/nickname-available").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/participants/join").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/participants/join/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/participants/submit-answer").permitAll()

                        // Session endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/sessions/*/join").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/sessions/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/sessions").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/sessions/**").hasAnyRole("ADMIN", "ORGANIZER")

                        // Media endpoints
                        .requestMatchers("/media/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/medias/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/medias/**").hasAnyRole("ADMIN", "ORGANIZER")

                        // Quiz endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/me").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/draft").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/favorite/me").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/feedback/me").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "api/vq/quizzes/{quizId}/favorite").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "api/vq/quizzes/{quizId}/favorite").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "api/v1/quizzes/{quizId}/feedback").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/quizzes/folk/**").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes/{quizId}/folk").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/quizzes/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/quizzes/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "api/v1/quizzes/admin/{quizId}/suspend").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/favorite").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/feedback").hasRole("ADMIN")

                        // Question endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/questions/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/questions/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/questions/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/questions/**").hasAnyRole("ADMIN", "ORGANIZER")

                        // User endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/me").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/{userId}").hasRole("ADMIN")

                        // Category endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasAnyRole("ADMIN", "ORGANIZER")

                        // Option endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/options/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/options/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/options/**").hasAnyRole("ADMIN", "ORGANIZER")

                        // Rating endpoints
                        .requestMatchers("/api/v1/sessions/*/report").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers("/api/v1/sessions/reports/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers("/api/v1/sessions/*/generate-report").hasAnyRole("ADMIN", "ORGANIZER")

                        // Leaderboard endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/leaderboard/session/*/initialize").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/leaderboard/session/*/finalize").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/leaderboard/history").hasAnyRole("ADMIN", "ORGANIZER")

                        // Rating endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/ratings/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ratings/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/ratings/**").hasAnyRole("ADMIN")

                        .anyRequest().authenticated()
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
