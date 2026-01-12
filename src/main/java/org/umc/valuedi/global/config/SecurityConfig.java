package org.umc.valuedi.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.umc.valuedi.global.apiPayload.code.GeneralErrorCode;
import org.umc.valuedi.global.security.handler.SecurityExceptionHandler;
import org.umc.valuedi.global.security.jwt.JwtAuthFilter;
import org.umc.valuedi.global.security.jwt.JwtUtil;
import org.umc.valuedi.global.security.service.CustomUserDetailsService;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final SecurityExceptionHandler securityExceptionHandler;
    private final CustomUserDetailsService customUserDetailsService;

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
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            securityExceptionHandler.sendErrorResponse(response, GeneralErrorCode.UNAUTHORIZED);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            securityExceptionHandler.sendErrorResponse(response, GeneralErrorCode.FORBIDDEN);
                        })
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtUtil, securityExceptionHandler, customUserDetailsService);
    }
}
