package org.dominik.pass.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dominik.pass.errors.api.ApiError;
import org.dominik.pass.security.filters.AccessFilter;
import org.dominik.pass.security.filters.LoginFilter;
import org.dominik.pass.security.filters.RefreshFilter;
import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile(value = {"dev", "test", "integration"})
public class SecurityConfigDev extends WebSecurityConfigurerAdapter {
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService detailsService;
  private final ObjectMapper mapper;
  private final RefreshTokenService tokenService;
  private final AccountService accountService;
  private final SecurityUtils securityUtils;
  private final JwtUtils jwtUtils;

  @Autowired
  public SecurityConfigDev(
      PasswordEncoder passwordEncoder,
      UserDetailsService detailsService,
      ObjectMapper mapper,
      RefreshTokenService tokenService,
      AccountService accountService,
      SecurityUtils securityUtils,
      JwtUtils jwtUtils
  ) {
    this.passwordEncoder = passwordEncoder;
    this.detailsService = detailsService;
    this.mapper = mapper;
    this.tokenService = tokenService;
    this.accountService = accountService;
    this.securityUtils = securityUtils;
    this.jwtUtils = jwtUtils;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeRequests(authorizeRequest -> authorizeRequest
            .antMatchers("/auth/signup", "/auth/signin", "/auth/salt", "/accounts/hint", "/dummy-url").permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(handler -> {
          handler.authenticationEntryPoint(authEntryPoint());
          handler.accessDeniedHandler(accessDeniedHandler());
        })
        .addFilter(createLoginFilter(
            authenticationManager(),
            mapper,
            tokenService,
            jwtUtils,
            authFailureHandler()
        ))
        .addFilterBefore(new RefreshFilter(mapper, jwtUtils, tokenService, securityUtils), LoginFilter.class)
        .addFilterAfter(new AccessFilter(accountService, jwtUtils, securityUtils), RefreshFilter.class);
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

  private LoginFilter createLoginFilter(
      AuthenticationManager authManager,
      ObjectMapper mapper,
      RefreshTokenService tokenService,
      JwtUtils jwtUtils,
      AuthenticationFailureHandler failureHandler
  ) {
    LoginFilter filter = new LoginFilter(authManager, mapper, tokenService, jwtUtils);
    AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/auth/signin", "POST");

    filter.setFilterProcessesUrl(requestMatcher.getPattern());
    filter.setAuthenticationFailureHandler(failureHandler);

    return filter;
  }

  private AuthenticationFailureHandler authFailureHandler() {
    return (req, res, e) -> {
      ApiError.ErrorBuilder apiErrorBuilder = ApiError
          .builder()
              .status(HttpStatus.UNAUTHORIZED)
                  .timestamp(Instant.now());

      if (e.getClass() == AuthenticationServiceException.class)
        apiErrorBuilder.message(e.getMessage());
      else
        apiErrorBuilder.message("Email or password invalid");

      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      res.getWriter().write(mapper.writeValueAsString(apiErrorBuilder.build()));
    };
  }

  private AuthenticationEntryPoint authEntryPoint() {
    return (req, res, e) -> {
      ApiError apiError = ApiError
          .builder()
          .status(HttpStatus.UNAUTHORIZED)
          .message("User must log in first")
          .timestamp(Instant.now())
          .build();

      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      res.getWriter().write(mapper.writeValueAsString(apiError));
    };
  }

  private AccessDeniedHandler accessDeniedHandler() {
    return (req, res, e) -> {
      ApiError apiError = ApiError
          .builder()
          .status(HttpStatus.FORBIDDEN)
          .message("You are not allowed to access this resource")
          .timestamp(Instant.now())
          .build();

      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      res.getWriter().write(mapper.writeValueAsString(apiError));
    };
  }
}
