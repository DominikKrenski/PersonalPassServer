package org.dominik.pass.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class LoginFilter extends UsernamePasswordAuthenticationFilter {
  private final AuthenticationManager authManager;
  private final ObjectMapper mapper;
  private final JwtUtils jwtUtils;

  public LoginFilter(AuthenticationManager authManager, ObjectMapper mapper, JwtUtils jwtUtils) {
    this.authManager = authManager;
    this.mapper = mapper;
    this.jwtUtils = jwtUtils;

    AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/auth/signin", "POST");
    setFilterProcessesUrl(requestMatcher.getPattern());
  }

  @SneakyThrows
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    if (!request.getMethod().equals("POST"))
      throw new AuthenticationServiceException("Authentication method not supported");

    Credentials creds = mapper.readValue(request.getInputStream(), Credentials.class);

    return authManager.authenticate(
        new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword())
    );
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
    AccountDetails details = (AccountDetails) authResult.getPrincipal();

    String accessToken = jwtUtils.createToken(details.getPublicId().toString(), JwtUtils.TokenType.ACCESS_TOKEN);
    String refreshToken = jwtUtils.createToken(details.getPublicId().toString(), JwtUtils.TokenType.REFRESH_TOKEN);

    AuthDTO authDTO = AuthDTO
        .builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();

    String json = mapper.writeValueAsString(authDTO);

    response.getWriter().write(json);
  }

  @Setter
  @Getter
  private static final class Credentials {
    private String email;
    private String password;
  }
}
