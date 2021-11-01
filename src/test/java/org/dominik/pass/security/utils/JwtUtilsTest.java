package org.dominik.pass.security.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.security.SignatureException;
import org.dominik.pass.configuration.JwtConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {
  private static final String ISSUER = "personal-pass.dev";
  private static final String SUBJECT = UUID.randomUUID().toString();
  private static final String KEY = "gUkXn2r5u8x/A?D(G+KbPeShVmYq3s6v9y$B&E)H@McQfTjWnZr4u7w!z%C*F-Ja";
  private static final String ACCESS_TOKEN_AUDIENCE = "access";
  private static final String REFRESH_TOKEN_AUDIENCE = "refresh";
  private static final long ISSUED_AT = Instant.now().getEpochSecond();
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

  @Test
  @DisplayName("should return subject")
  void shouldReturnSubject() {
    JwtConfig.JwtToken accessToken = new JwtConfig.JwtToken();
    accessToken.setAudience(ACCESS_TOKEN_AUDIENCE);
    accessToken.setExpiration(ACCESS_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getAccessToken()).thenReturn(accessToken);

    String jwt = jwtUtils.createToken(SUBJECT, JwtUtils.TokenType.ACCESS_TOKEN);

    String subject = jwtUtils.readSubject(jwt, JwtUtils.TokenType.ACCESS_TOKEN);

    assertEquals(SUBJECT, subject);
  }

  @Test
  @DisplayName("should return access token audience")
  void shouldReturnAccessTokenAudience() {
    JwtConfig.JwtToken accessToken = new JwtConfig.JwtToken();
    accessToken.setAudience(ACCESS_TOKEN_AUDIENCE);
    accessToken.setExpiration(ACCESS_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getAccessToken()).thenReturn(accessToken);

    String jwt = jwtUtils.createToken(SUBJECT, JwtUtils.TokenType.ACCESS_TOKEN);

    String audience = jwtUtils.readAudience(jwt, JwtUtils.TokenType.ACCESS_TOKEN);

    assertEquals(ACCESS_TOKEN_AUDIENCE, audience);
  }

  @Test
  @DisplayName("should return refresh token audience")
  void shouldReturnRefreshTokenAudience() {
    JwtConfig.JwtToken refreshToken = new JwtConfig.JwtToken();
    refreshToken.setAudience(REFRESH_TOKEN_AUDIENCE);
    refreshToken.setExpiration(REFRESH_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getRefreshToken()).thenReturn(refreshToken);

    String jwt = jwtUtils.createToken(SUBJECT, JwtUtils.TokenType.REFRESH_TOKEN);

    String audience = jwtUtils.readAudience(jwt, JwtUtils.TokenType.REFRESH_TOKEN);

    assertEquals(REFRESH_TOKEN_AUDIENCE, audience);
  }

  @Test
  @DisplayName("should throw IncorrectClaimException if required audience is refresh and access token was send")
  void shouldThrowIncorrectClaimExceptionIfAudienceMustBeRefreshAndAccessTokenWasSend() {
    JwtConfig.JwtToken accessToken = new JwtConfig.JwtToken();
    accessToken.setAudience(ACCESS_TOKEN_AUDIENCE);
    accessToken.setExpiration(ACCESS_TOKEN_EXPIRATION);

    JwtConfig.JwtToken refreshToken = new JwtConfig.JwtToken();
    refreshToken.setAudience(REFRESH_TOKEN_AUDIENCE);
    refreshToken.setExpiration(REFRESH_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getAccessToken()).thenReturn(accessToken);

    when(jwtConfig.getRefreshToken()).thenReturn(refreshToken);

    String jwt = jwtUtils.createToken(SUBJECT, JwtUtils.TokenType.ACCESS_TOKEN);

    assertThrows(IncorrectClaimException.class, () -> jwtUtils.readAudience(jwt, JwtUtils.TokenType.REFRESH_TOKEN));
  }

  @Test
  @DisplayName("should throw IncorrectClaimException if audience must be access and refresh token was send")
  void shouldThrowIncorrectClaimExceptionIfAudienceMustBeAccessAndRefreshTokenWasSend() {
    JwtConfig.JwtToken refreshToken = new JwtConfig.JwtToken();
    refreshToken.setAudience(REFRESH_TOKEN_AUDIENCE);
    refreshToken.setExpiration(REFRESH_TOKEN_EXPIRATION);

    JwtConfig.JwtToken accessToken = new JwtConfig.JwtToken();
    accessToken.setAudience(ACCESS_TOKEN_AUDIENCE);
    accessToken.setExpiration(ACCESS_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getRefreshToken()).thenReturn(refreshToken);

    when(jwtConfig.getAccessToken()).thenReturn(accessToken);

    String jwt = jwtUtils.createToken(SUBJECT, JwtUtils.TokenType.REFRESH_TOKEN);

    assertThrows(IncorrectClaimException.class, () -> jwtUtils.readAudience(jwt, JwtUtils.TokenType.ACCESS_TOKEN));
  }

  @Test
  @DisplayName("should throw MissingClaimException if issuer is missing")
  void shouldThrowMissingClaimExceptionIfIssuerIsMissing() throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
    JwtConfig.JwtToken accessToken = new JwtConfig.JwtToken();
    accessToken.setAudience(ACCESS_TOKEN_AUDIENCE);
    accessToken.setExpiration(ACCESS_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getAccessToken()).thenReturn(accessToken);

    String jwt = generateJwtToken(null, SUBJECT, ISSUED_AT, ACCESS_TOKEN_AUDIENCE, ISSUED_AT + ACCESS_TOKEN_EXPIRATION, KEY);

    assertThrows(MissingClaimException.class, () -> jwtUtils.readAudience(jwt, JwtUtils.TokenType.ACCESS_TOKEN));
  }

  @Test
  @DisplayName("should throw MissingClaimException if audience is missing")
  void shouldThrowMissingClaimExceptionIfAudienceIsMissing() throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
    JwtConfig.JwtToken refreshToken = new JwtConfig.JwtToken();
    refreshToken.setAudience(REFRESH_TOKEN_AUDIENCE);
    refreshToken.setExpiration(REFRESH_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getRefreshToken()).thenReturn(refreshToken);

    String jwt = generateJwtToken(ISSUER, SUBJECT, ISSUED_AT, null, ISSUED_AT + REFRESH_TOKEN_EXPIRATION, KEY);

    assertThrows(MissingClaimException.class, () -> jwtUtils.readAudience(jwt, JwtUtils.TokenType.REFRESH_TOKEN));
  }

  @Test
  @DisplayName("should throw ExpiredJwtException")
  void shouldThrowExpiredJwtException() throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
    JwtConfig.JwtToken accessToken = new JwtConfig.JwtToken();
    accessToken.setAudience(ACCESS_TOKEN_AUDIENCE);
    accessToken.setExpiration(ACCESS_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getAccessToken()).thenReturn(accessToken);

    String jwt = generateJwtToken(ISSUER, SUBJECT, ISSUED_AT, ACCESS_TOKEN_AUDIENCE, ISSUED_AT - ACCESS_TOKEN_EXPIRATION, KEY);

    assertThrows(ExpiredJwtException.class, () -> jwtUtils.readSubject(jwt, JwtUtils.TokenType.ACCESS_TOKEN));
  }

  @Test
  @DisplayName("should throw SignatureException")
  void shouldThrowSignatureException() throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
    JwtConfig.JwtToken refreshToken = new JwtConfig.JwtToken();
    refreshToken.setAudience(REFRESH_TOKEN_AUDIENCE);
    refreshToken.setExpiration(REFRESH_TOKEN_EXPIRATION);

    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getKey()).thenReturn(KEY);
    when(jwtConfig.getRefreshToken()).thenReturn(refreshToken);

    String jwt = generateJwtToken(ISSUER, SUBJECT, ISSUED_AT, REFRESH_TOKEN_AUDIENCE, ISSUED_AT + REFRESH_TOKEN_EXPIRATION, KEY);
    String[] jwtArray = jwt.split("\\.");

    jwtArray[2] = jwtArray[2].substring(1);
    String malformedJwt = String.join(".", jwtArray);

    assertThrows(SignatureException.class, () -> jwtUtils.readSubject(malformedJwt, JwtUtils.TokenType.REFRESH_TOKEN));
  }
}
