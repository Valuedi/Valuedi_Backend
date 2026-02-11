package org.umc.valuedi.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
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
import org.umc.valuedi.global.apiPayload.code.GeneralErrorCode;
import org.umc.valuedi.global.security.handler.SecurityExceptionHandler;
import org.umc.valuedi.global.security.jwt.JwtAuthFilter;
import org.umc.valuedi.global.security.jwt.JwtUtil;
import org.umc.valuedi.global.security.service.CustomUserDetailsService;

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
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

            // Terms APIs
            "/api/terms",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(allowUris).permitAll()
                        // 사용자 기능 추가되면 활성화
//                        .requestMatchers("/api/trophies", "/api/members/me/trophies").hasAuthority(Role.ROLE_USER.name())
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtUtil, redisTemplate, securityExceptionHandler, customUserDetailsService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "https://valuedi-web.vercel.app",
                "http://localhost:5173",
                "https://api.valuedi.site",
                "http://localhost:8080"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
