package org.dominik.pass.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createObjectMapperInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthDtoSerializationTest {
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String ACCESS_TOKEN = "access_token";
  private static final String REFRESH_TOKEN = "refresh_token";

  private static ObjectMapper mapper;

  @BeforeAll
  static void setUp() {
    mapper = createObjectMapperInstance();
  }

  @Test
  @DisplayName("should serialize all fields")
  void shouldSerializeAllFields() throws JsonProcessingException {
    AuthDTO dto = AuthDTO
        .builder()
        .publicId(PUBLIC_ID)
        .accessToken(ACCESS_TOKEN)
        .refreshToken(REFRESH_TOKEN)
        .build();

    String json = mapper.writeValueAsString(dto);

    ReadContext ctx = JsonPath.parse(json);

    assertEquals(PUBLIC_ID.toString(), ctx.read("$.publicId"));
    assertEquals(ACCESS_TOKEN, ctx.read("$.accessToken"));
    assertEquals(REFRESH_TOKEN, ctx.read("$.refreshToken"));
  }

  @Test
  @DisplayName("should serialize only public id")
  void shouldSerializeOnlyPublicId() throws JsonProcessingException {
    AuthDTO dto = AuthDTO
        .builder()
        .publicId(PUBLIC_ID)
        .build();

    String json = mapper.writeValueAsString(dto);

    ReadContext ctx = JsonPath.parse(json);

    assertEquals(PUBLIC_ID.toString(), ctx.read("$.publicId"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.accessToken"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.refreshToken"));
  }

  @Test
  @DisplayName("should serialize only access token")
  void shouldSerializeOnlyAccessToken() throws JsonProcessingException {
    AuthDTO dto = AuthDTO
        .builder()
        .accessToken(ACCESS_TOKEN)
        .build();

    String json = mapper.writeValueAsString(dto);

    ReadContext ctx = JsonPath.parse(json);

    assertThrows(PathNotFoundException.class, () -> ctx.read("$.publicId"));
    assertEquals(ACCESS_TOKEN, ctx.read("$.accessToken"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.refreshToken"));
  }

  @Test
  @DisplayName("should serialize only refresh token")
  void shouldSerializeOnlyRefreshToken() throws JsonProcessingException {
    AuthDTO dto = AuthDTO
        .builder()
        .refreshToken(REFRESH_TOKEN)
        .build();

    String json = mapper.writeValueAsString(dto);

    ReadContext ctx = JsonPath.parse(json);

    assertThrows(PathNotFoundException.class, () -> ctx.read("$.publicId"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.accessToken"));
    assertEquals(REFRESH_TOKEN, ctx.read("$.refreshToken"));
  }

  @Test
  @DisplayName("should serialize access and refresh tokens")
  void shouldSerializeAccessAndRefreshTokens() throws JsonProcessingException {
    AuthDTO dto = AuthDTO
        .builder()
        .accessToken(ACCESS_TOKEN)
        .refreshToken(REFRESH_TOKEN)
        .build();

    String json = mapper.writeValueAsString(dto);

    ReadContext ctx = JsonPath.parse(json);

    assertThrows(PathNotFoundException.class, () -> ctx.read("$.publicId"));
    assertEquals(ACCESS_TOKEN, ctx.read("$.accessToken"));
    assertEquals(REFRESH_TOKEN, ctx.read("$.refreshToken"));
  }
}
