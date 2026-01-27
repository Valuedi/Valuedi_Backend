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
        } catch (ExpiredJwtException e) {
            String uri = request.getRequestURI();

            // 로그인 상태 조회는 그냥 필터 통과
            if (uri.equals("/auth/status")) {
                filterChain.doFilter(request, response);
                return;
            }

            // 로그아웃과 토큰 재발급의 경우 인증 객체 생성 후 필터 통과
            if(uri.equals("/auth/logout") || uri.equals("/auth/token/refresh")) {
                Claims claims = e.getClaims();
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

            // 위의 경우가 아니면 에러 응답
            securityExceptionHandler.sendErrorResponse(response, AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            String uri = request.getRequestURI();

            // 로그인 상태 조회는 그냥 필터 통과
            if (uri.equals("/auth/status")) {
                filterChain.doFilter(request, response);
                return;
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
