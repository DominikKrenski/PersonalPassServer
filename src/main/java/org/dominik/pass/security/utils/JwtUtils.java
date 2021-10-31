package org.dominik.pass.security.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import org.dominik.pass.configuration.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

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

    switch (tokenType) {
      case ACCESS_TOKEN -> {
        builder.setAudience(jwtConfig.getAccessToken().getAudience());
        builder.setExpiration(Date.from(now.plusSeconds(jwtConfig.getAccessToken().getExpiration())));
      }
      case REFRESH_TOKEN -> {
        builder.setAudience(jwtConfig.getRefreshToken().getAudience());
        builder.setExpiration(Date.from(now.plusSeconds(jwtConfig.getRefreshToken().getExpiration())));
      }
    }

    return builder.signWith(secretKey).compact();
  }
}
