package com.spring.boot.example.mybatis.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    private final TokenProvider tokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        Optional<String> jwtToken = resolveToken(httpServletRequest);

        if (jwtToken.isPresent() && tokenProvider.validateToken(jwtToken.get())) {
            Authentication authentication = tokenProvider.getAuthentication(jwtToken.get());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        };

        chain.doFilter(request, response);
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(bearerToken -> StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer "))
                .map(bearerToken -> bearerToken.substring(7));
    }

}
