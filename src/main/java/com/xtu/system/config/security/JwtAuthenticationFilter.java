package com.xtu.system.config.security;

import com.xtu.system.common.exception.BusinessException;
import com.xtu.system.modules.system.menu.mapper.MenuMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MenuMapper menuMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, MenuMapper menuMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.menuMapper = menuMapper;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            try {
                String token = jwtTokenProvider.resolveToken(authorizationHeader);
                if (jwtTokenProvider.isValid(token)) {
                    AuthenticatedUser authenticatedUser = jwtTokenProvider.getAuthenticatedUser(token);
                    List<SimpleGrantedAuthority> authorities = menuMapper.selectPermissionCodesByUserId(authenticatedUser.getUserId())
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (BusinessException exception) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
