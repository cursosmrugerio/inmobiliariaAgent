package com.inmobiliaria.gestion.auth.security;

import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.auth.service.UserAccountService;
import com.inmobiliaria.gestion.exception.JwtValidationException;
import com.inmobiliaria.gestion.exception.ResourceNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtUtil jwtUtil;
  private final UserAccountService userAccountService;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, UserAccountService userAccountService) {
    this.jwtUtil = jwtUtil;
    this.userAccountService = userAccountService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        JwtPayload payload = jwtUtil.validateToken(token);
        UserAccount user = userAccountService.getById(payload.getUserId());
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                user, null, buildAuthorities(payload.getRole()));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtValidationException | ResourceNotFoundException ex) {
        log.warn("Token JWT inválido: {}", ex.getMessage());
        // Return 401 Unauthorized for expired/invalid tokens
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response
            .getWriter()
            .write("{\"error\":\"Unauthorized\",\"message\":\"" + ex.getMessage() + "\"}");
        return; // Stop filter chain
      }
    }

    filterChain.doFilter(request, response);
  }

  private List<GrantedAuthority> buildAuthorities(String role) {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }
}
