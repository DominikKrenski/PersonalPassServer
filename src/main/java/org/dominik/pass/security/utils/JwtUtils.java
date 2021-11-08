package org.dominik.pass.security.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import org.dominik.pass.configuration.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Component
public final class JwtUtils {
  public enum TokenType {
    ACCESS_TOKEN,
    REFRESH_TOKEN
  }

  private final JwtConfig jwtConfig;

  @Autowired
  public JwtUtils(JwtConfig jwtConfig) {
    this.jwtConfig = jwtConfig;
  }

  public String createToken(@NonNull String subject, @NonNull TokenType tokenType) {
    SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfig.getKey().getBytes(StandardCharsets.UTF_8));

    Instant now = Instant.now();

    JwtBuilder builder = Jwts
        .builder()
        .setIssuer(jwtConfig.getIssuer())
        .setSubject(subject)
        .setIssuedAt(Date.from(now));

    if (tokenType == TokenType.ACCESS_TOKEN) {
      builder.setAudience(jwtConfig.getAccessToken().getAudience());
      builder.setExpiration(Date.from(now.plusSeconds(jwtConfig.getAccessToken().getExpiration())));
    } else {
      builder.setAudience(jwtConfig.getRefreshToken().getAudience());
      builder.setExpiration(Date.from(now.plusSeconds(jwtConfig.getRefreshToken().getExpiration())));
    }

    return builder.signWith(secretKey).compact();
  }

  public String readSubject(@NonNull String token, TokenType tokenType) {
    return readClaim(token, tokenType, Claims::getSubject);
  }

  public String readAudience(@NonNull String token, TokenType tokenType) {
    return readClaim(token, tokenType, Claims::getAudience);
  }

  private Claims readAllClaims(@NonNull String token, @NonNull TokenType tokenType) {
    SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfig.getKey().getBytes(StandardCharsets.UTF_8));
    JwtParserBuilder builder = Jwts.parserBuilder();

    builder.setSigningKey(secretKey);
    builder.requireIssuer(jwtConfig.getIssuer());

    if (tokenType == TokenType.ACCESS_TOKEN)
      builder.requireAudience(jwtConfig.getAccessToken().getAudience());
    else
      builder.requireAudience(jwtConfig.getRefreshToken().getAudience());

    return builder.build().parseClaimsJws(token).getBody();
  }

  private <T> T readClaim(@NonNull String token, TokenType tokenType, Function<Claims, T> getClaim) {
    Claims claims = readAllClaims(token, tokenType);
    return getClaim.apply(claims);

    // MissingClaimException -> parsed JWT did not have field
    // IncorrectClaimException -> parsed JWT had a field, but its value was incorrect
    // ExpiredJwtException -> token is expired
    // SignatureException -> signature is not valid
    // MalformedJwtException -> ???
    // UnsupportedJwtException -> ???
  }
}
