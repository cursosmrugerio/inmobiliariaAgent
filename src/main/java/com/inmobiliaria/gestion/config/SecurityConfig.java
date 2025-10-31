package com.inmobiliaria.gestion.config;

import com.inmobiliaria.gestion.auth.security.JwtAuthenticationFilter;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${cors.allowed-origins}")
  private String allowedOrigins;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    return http.csrf(csrf -> csrf.disable()) // Disable CSRF for stateless JWT authentication
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                    .permitAll()
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/assets/**",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.svg",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.ico",
                        "/vite.svg",
                        "/favicon.ico",
                        "/login",
                        "/chat",
                        "/dashboard",
                        "/inmobiliarias",
                        "/inmobiliarias/**",
                        "/propiedades",
                        "/propiedades/**",
                        "/personas",
                        "/personas/**")
                    .permitAll()
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**")
                    .permitAll()
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    .requestMatchers("/api/agent/**")
                    .authenticated() // ✅ Authentication enabled for production security
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(form -> form.disable())
        .logout(logout -> logout.disable())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // Parse comma-separated origins from environment variable
    // Supports: "http://localhost:5173,http://localhost:8080,https://your-domain.com"
    List<String> origins = Arrays.asList(allowedOrigins.split(","));
    config.setAllowedOrigins(origins);

    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
