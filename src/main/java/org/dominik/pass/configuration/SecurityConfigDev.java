package org.dominik.pass.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dominik.pass.security.entries.AuthEntryPoint;
import org.dominik.pass.security.filters.LoginFilter;
import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("dev")
public class SecurityConfigDev extends WebSecurityConfigurerAdapter {
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService detailsService;
  private final ObjectMapper mapper;
  private final RefreshTokenService tokenService;
  private final JwtUtils jwtUtils;

  @Autowired
  public SecurityConfigDev(
      PasswordEncoder passwordEncoder,
      UserDetailsService detailsService,
      ObjectMapper mapper,
      RefreshTokenService tokenService,
      JwtUtils jwtUtils
  ) {
    this.passwordEncoder = passwordEncoder;
    this.detailsService = detailsService;
    this.mapper = mapper;
    this.tokenService = tokenService;
    this.jwtUtils = jwtUtils;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeRequests(authorizeRequest -> authorizeRequest
            .antMatchers("/auth/signup", "/auth/signin", "/auth/salt").permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(handler -> handler.authenticationEntryPoint(new AuthEntryPoint(mapper)))
        .addFilter(new LoginFilter(authenticationManager(),mapper, tokenService, jwtUtils));
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
        .userDetailsService(detailsService)
        .passwordEncoder(passwordEncoder);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://personal-pass.dev"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Content-Length"));
    config.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
