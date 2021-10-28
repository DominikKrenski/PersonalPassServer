package org.dominik.pass.data.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AuthDtoCreationTest {
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String ACCESS_TOKEN = "access_token";
  private static final String REFRESH_TOKEN = "refresh_token";

  @Test
  @DisplayName("should create instance if all fields were set")
  void shouldCreateInstanceIfAllFieldsWereSet() {
    AuthDTO dto = AuthDTO
        .builder()
        .publicId(PUBLIC_ID)
        .accessToken(ACCESS_TOKEN)
        .refreshToken(REFRESH_TOKEN)
        .build();

    assertEquals(PUBLIC_ID.toString(), dto.getPublicId().toString());
    assertEquals(ACCESS_TOKEN, dto.getAccessToken());
    assertEquals(REFRESH_TOKEN, dto.getRefreshToken());
  }

  @Test
  @DisplayName("should create instance if only public id was set")
  void shouldCreateInstanceIfOnlyPublicIdWasSet() {
    AuthDTO dto = AuthDTO
        .builder()
        .publicId(PUBLIC_ID)
        .build();

    assertEquals(PUBLIC_ID.toString(), dto.getPublicId().toString());
    assertNull(dto.getAccessToken());
    assertNull(dto.getRefreshToken());
  }

  @Test
  @DisplayName("should create instance if only access token was set")
  void shouldCreateInstanceIfOnlyAccessTokenWasSet() {
    AuthDTO dto = AuthDTO
        .builder()
        .accessToken(ACCESS_TOKEN)
        .build();

    assertNull(dto.getPublicId());
    assertEquals(ACCESS_TOKEN, dto.getAccessToken());
    assertNull(dto.getRefreshToken());
  }

  @Test
  @DisplayName("should create instance if only refresh token was set")
  void shouldCreateInstanceIfOnlyRefreshTokenWasSet() {
    AuthDTO dto = AuthDTO
        .builder()
        .refreshToken(REFRESH_TOKEN)
        .build();

    assertNull(dto.getPublicId());
    assertNull(dto.getAccessToken());
    assertEquals(REFRESH_TOKEN, dto.getRefreshToken());
  }
}
