package org.dominik.pass.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Locale;

public final class LoginFilter extends UsernamePasswordAuthenticationFilter {
  private final AuthenticationManager authManager;
  private final ObjectMapper mapper;
  private final RefreshTokenService tokenService;
  private final JwtUtils jwtUtils;

  public LoginFilter(
    AuthenticationManager authManager,
    ObjectMapper mapper,
    RefreshTokenService tokenService,
    JwtUtils jwtUtils
  ) {
    this.authManager = authManager;
    this.mapper = mapper;
    this.tokenService = tokenService;
    this.jwtUtils = jwtUtils;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    if (!request.getMethod().equalsIgnoreCase("POST"))
      throw new AuthenticationServiceException("Authentication method not supported");

    if (!request.getContentType().toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE.toLowerCase(Locale.ROOT)))
      throw new AuthenticationServiceException("Content-TYpe not supported");

    Credentials creds;

    try {
      creds = mapper.readValue(request.getInputStream(), Credentials.class);
    } catch (IOException ex) {
      throw new AuthenticationServiceException("There is a problem with request data");
    }

    return authManager.authenticate(
      new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword())
    );
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
    AccountDetails details = (AccountDetails) authResult.getPrincipal();

    // generate token pair
    String accessToken = jwtUtils.createToken(details.getPublicId().toString(), JwtUtils.TokenType.ACCESS_TOKEN);
    String refreshToken = jwtUtils.createToken(details.getPublicId().toString(), JwtUtils.TokenType.REFRESH_TOKEN);

    //generate new secure key for master key encryption/decryption
    String key = generateSecureKeyHex();

    // save new refresh token in database
    tokenService.login(refreshToken, details.getUsername(), key);

    AuthDTO authDTO = AuthDTO
      .builder()
      .key(key)
      .accessToken(accessToken)
      .refreshToken(refreshToken)
      .build();

    String json = mapper.writeValueAsString(authDTO);

    response.getWriter().write(json);
  }

  private String generateSecureKeyHex() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);

    return convertByteArrayToHex(bytes);
  }

  private String convertByteArrayToHex(byte[] arr) {
    StringBuilder builder = new StringBuilder();

    for (byte i : arr)
      builder.append(String.format("%02X", i));

    return builder.toString();
  }

  @Setter
  @Getter
  private static final class Credentials {
    private String email;
    private String password;
  }
}
