package org.umc.valuedi.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.global.apiPayload.code.GeneralErrorCode;
import org.umc.valuedi.global.security.handler.SecurityExceptionHandler;
import org.umc.valuedi.global.security.service.CustomUserDetailsService;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final SecurityExceptionHandler securityExceptionHandler;
    private final CustomUserDetailsService customUserDetailsService;

    private static final String AUTH_STATUS_URI = "/auth/status";
    private static final String LOGOUT_URI = "/auth/logout";
    private static final String REFRESH_URI = "/auth/token/refresh";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            token = token.replace("Bearer ", "");
            String isBlacklisted = redisTemplate.opsForValue().get("BL:" + token);

            if(isBlacklisted != null) {
                securityExceptionHandler.sendErrorResponse(response, AuthErrorCode.TOKEN_IN_BLACKLIST);
                return;
            }

            String category = jwtUtil.getCategory(token);

            if(!category.equals("access")) {
                securityExceptionHandler.sendErrorResponse(response, AuthErrorCode.NOT_ACCESS_TOKEN);
                return;
            }

            String memberId = jwtUtil.getMemberId(token);
            setAuthenticationContext(memberId);

            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            String uri = request.getRequestURI();

            // 로그인 상태 조회는 토큰이 만료되거나 유효하지 않아도 필터 통과
            if (uri.equals(AUTH_STATUS_URI)) {
                filterChain.doFilter(request, response);
                return;
            }

            if(e instanceof ExpiredJwtException expiredException) {
                // 로그아웃과 토큰 재발급의 경우 만료된 토큰만 필터 통과
                if(uri.equals(LOGOUT_URI) || uri.equals(REFRESH_URI)) {
                    Claims claims = expiredException.getClaims();
                    String category = claims.get("category", String.class);

                    // 만료된 토큰이어도 엑세스 토큰이 맞는지 확인
                    if (!category.equals("access")) {
                        securityExceptionHandler.sendErrorResponse(response, AuthErrorCode.NOT_ACCESS_TOKEN);
                        return;
                    }

                    setAuthenticationContext(claims.getSubject());
                    filterChain.doFilter(request, response);
                    return;
                }
            }
            securityExceptionHandler.sendErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
        } catch (Exception e) {
            securityExceptionHandler.sendErrorResponse(response, GeneralErrorCode.UNAUTHORIZED);
        }
    }

    // 인증 객체 저장
    private void setAuthenticationContext(String memberId) {
        UserDetails user = customUserDetailsService.loadUserByUsername(memberId);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
