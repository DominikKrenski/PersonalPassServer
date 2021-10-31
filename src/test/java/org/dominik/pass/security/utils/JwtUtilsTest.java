package org.dominik.pass.security.utils;

import org.dominik.pass.configuration.JwtConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {
  private static final String ISSUER = "personal-pass.dev";
  private static final String SUBJECT = UUID.randomUUID().toString();
  private static final String KEY = "gUkXn2r5u8x/A?D(G+KbPeShVmYq3s6v9y$B&E)H@McQfTjWnZr4u7w!z%C*F-Ja";
  private static final String ACCESS_TOKEN_AUDIENCE = "access";
  private static final String REFRESH_TOKEN_AUDIENCE = "refresh";
  private static final int ACCESS_TOKEN_EXPIRATION = 120;
  private static final int REFRESH_TOKEN_EXPIRATION = 300;

  @Mock JwtConfig jwtConfig;
  @InjectMocks JwtUtils jwtUtils;

  @Test
  @DisplayName("should create access token instance")
  void shouldCreateAccessTokenInstance() throws Exception {
    JwtConfig.JwtToken accessToken = new JwtConfig.JwtToken();
    accessToken.setAudience(ACCESS_TOKEN_AUDIENCE);
    accessToken.setExpiration(ACCESS_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getAccessToken()).thenReturn(accessToken);

    String jwt = jwtUtils.createToken(SUBJECT, JwtUtils.TokenType.ACCESS_TOKEN);

    String regex = "(.*)(\"iat\":\\s?\\d{10,})(.*)";
    Pattern pattern = Pattern.compile(regex);

    Matcher matcher = pattern.matcher(base64UrlDecode(jwt.split("\\.")[1]));

    if (matcher.find()) {
      int issuedAt = Integer.parseInt(matcher.group(2).split(":")[1]);
      String token = generateJwtToken(ISSUER, SUBJECT, issuedAt, ACCESS_TOKEN_AUDIENCE, issuedAt + ACCESS_TOKEN_EXPIRATION, KEY);

      assertEquals(jwt, token);
    } else {
      throw new Exception("No matches found");
    }
  }

  @Test
  @DisplayName("should create refresh token instance")
  void shouldCreateRefreshTokenInstance() throws Exception {
    JwtConfig.JwtToken refreshToken = new JwtConfig.JwtToken();
    refreshToken.setAudience(REFRESH_TOKEN_AUDIENCE);
    refreshToken.setExpiration(REFRESH_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getRefreshToken()).thenReturn(refreshToken);

    String jwt = jwtUtils.createToken(SUBJECT, JwtUtils.TokenType.REFRESH_TOKEN);

    String regex = "(.*)(\"iat\":\\s?\\d{10,})(.*)";
    Pattern pattern = Pattern.compile(regex);

    Matcher matcher = pattern.matcher(base64UrlDecode(jwt.split("\\.")[1]));

    if (matcher.find()) {
      int issuedAt = Integer.parseInt(matcher.group(2).split(":")[1]);
      String token = generateJwtToken(ISSUER, SUBJECT, issuedAt, REFRESH_TOKEN_AUDIENCE, issuedAt + REFRESH_TOKEN_EXPIRATION, KEY);

      assertEquals(jwt, token);
    } else {
      throw new Exception("No matches found");
    }
  }
}
