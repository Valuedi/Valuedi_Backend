package org.umc.valuedi.global.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
            String category = jwtUtil.getCategory(token);

            if(!category.equals("access")) {
                securityExceptionHandler.sendErrorResponse(response, AuthErrorCode.NOT_ACCESS_TOKEN);
                return;
            }

            String memberId = jwtUtil.getMemberId(token);
            UserDetails user = customUserDetailsService.loadUserByUsername(memberId);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            securityExceptionHandler.sendErrorResponse(response, AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            securityExceptionHandler.sendErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
        } catch (Exception e) {
            securityExceptionHandler.sendErrorResponse(response, GeneralErrorCode.UNAUTHORIZED);
        }
    }
}
