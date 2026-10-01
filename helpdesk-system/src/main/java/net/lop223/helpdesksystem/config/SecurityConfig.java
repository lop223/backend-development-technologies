package net.lop223.helpdesksystem.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/tickets/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri(baseUrl + "/");

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/css/**", "/js/**", "/webjars/**").permitAll()
                        .requestMatchers("/ui/categories", "/ui/users").hasRole("ADMIN")
                        .requestMatchers("/ui/tickets/new").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/ui/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(userAuthoritiesMapper())
                        )
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler)
                );
        return http.build();
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository,
                                                         @Value("${app.base-url:http://localhost:8081}") String baseUrl) {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri(baseUrl + "/");
        return handler;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthoritiesFromJwt);
        return converter;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthoritiesFromJwt(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List<?> roles) {
                authorities.addAll(
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
                                .collect(Collectors.toSet())
                );
            }
        }
        return authorities;
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mapped = new HashSet<>(authorities); // зберігаємо OIDC_USER та SCOPE_*

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    if (oidcUserAuthority.getIdToken() != null) {
                        mapped.addAll(extractRealmRoles(oidcUserAuthority.getIdToken().getClaims()));
                    }
                    if (oidcUserAuthority.getUserInfo() != null) {
                        mapped.addAll(extractRealmRoles(oidcUserAuthority.getUserInfo().getClaims()));
                    }
                } else if (authority instanceof OAuth2UserAuthority oAuth2UserAuthority) {
                    mapped.addAll(extractRealmRoles(oAuth2UserAuthority.getAttributes()));
                }
            });

            return mapped;
        };
    }

    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractRealmRoles(Map<String, Object> claims) {
        Set<GrantedAuthority> result = new HashSet<>();
        Object realmAccessObj = claims.get("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccess) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List<?> roles) {
                for (Object role : roles) {
                    result.add(new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()));
                }
            }
        }
        return result;
    }
}