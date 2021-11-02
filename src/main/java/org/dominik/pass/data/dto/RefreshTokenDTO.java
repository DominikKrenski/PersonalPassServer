package org.dominik.pass.data.dto;

import lombok.*;
import org.dominik.pass.db.entities.RefreshToken;

import java.time.Instant;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@ToString
public final class RefreshTokenDTO {
  private final Long id;
  @EqualsAndHashCode.Include private final String token;
  private final AccountDTO account;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final short version;

  public static RefreshTokenDTO fromRefreshTokenLazy(@NonNull RefreshToken refreshToken) {
    return new RefreshTokenDTO(
        refreshToken.getId(),
        refreshToken.getToken(),
        null,
        refreshToken.getCreatedAt(),
        refreshToken.getUpdatedAt(),
        refreshToken.getVersion()
    );
  }

  public static RefreshTokenDTO fromRefreshTokenEager(@NonNull RefreshToken refreshToken) {
    return new RefreshTokenDTO(
        refreshToken.getId(),
        refreshToken.getToken(),
        AccountDTO.fromAccount(refreshToken.getAccount()),
        refreshToken.getCreatedAt(),
        refreshToken.getUpdatedAt(),
        refreshToken.getVersion()
    );
  }
}
