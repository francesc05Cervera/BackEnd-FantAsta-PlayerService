package com.example.fantasta.player_service.Config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.fantasta.player_service.Client.AuthServiceClient;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthServiceClient authServiceClient,
            @Value("${app.cors.allowed-origins:http://localhost:4200}") List<String> origins) throws Exception {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origins);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        return http.csrf(csrf -> csrf.disable()).cors(config -> config.configurationSource(source))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new AuthServiceAuthenticationFilter(authServiceClient), AnonymousAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/ping").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()).build();
    }
}
