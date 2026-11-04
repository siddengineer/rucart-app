package com.rucart.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public Authentication endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/payments/webhook").permitAll()

                        // Public browsing endpoints (catalogs, categories, stores)
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stores/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/stores/assign").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/stores/**").hasAnyRole("STORE_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/inventory/validate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/offers/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/banners/**").permitAll()

                        // Product management (Store Owner and Admin)
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("STORE_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyRole("STORE_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasAnyRole("STORE_OWNER", "ADMIN")

                        // Role-based Access Control
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/store-owner/**").hasAnyRole("STORE_OWNER", "ADMIN")
                        .requestMatchers("/api/v1/delivery/**").hasAnyRole("DELIVERY_PARTNER", "ADMIN")

                        // Authenticated endpoints (Orders, Addresses, Notifications, Profile)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
