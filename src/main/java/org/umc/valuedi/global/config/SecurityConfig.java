package org.umc.valuedi.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final String[] allowUris = {
            // Swagger & API Docs
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",

            // Health Check & Monitoring
            "/actuator/health",

            // Public Auth APIs (로그아웃 제외)
            "/auth/oauth/kakao/**",
            "/auth/login",
            "/auth/check-username",
            "/auth/token/refresh",
            "/auth/signup",
            "/auth/email/**",
            "/auth/status",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(allowUris).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
<<<<<<< HEAD
        return http.build();
    }
=======

        return http.build();
    }

>>>>>>> develop
}
