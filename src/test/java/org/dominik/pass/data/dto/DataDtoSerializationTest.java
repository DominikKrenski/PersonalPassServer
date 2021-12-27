package org.dominik.pass.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class DataDtoSerializationTest {
  private static final Long ACCOUNT_ID = 1L;
  private static final Long DATA_ID = 3L;
  private static final UUID ACCOUNT_PUBLIC_ID = UUID.randomUUID();
  private static final UUID DATA_PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_USER;
  private static final Instant ACCOUNT_CREATED_AT = Instant.now().minusSeconds(3000);
  private static final Instant ACCOUNT_UPDATED_AT = Instant.now().minusSeconds(2000);
  private static final Instant DATA_CREATED_AT = Instant.now().minusSeconds(2000);
  private static final Instant DATA_UPDATED_AT = Instant.now().minusSeconds(200);
  private static final short ACCOUNT_VERSION = 1;
  private static final short DATA_VERSION = 2;
  private static final String ENTRY = "entry";

  private static Account account;
  private static ObjectMapper mapper;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    account = createAccountInstance(
        ACCOUNT_ID,
        ACCOUNT_PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        true,
        true,
        true,
        true,
        ACCOUNT_CREATED_AT,
        ACCOUNT_UPDATED_AT,
        ACCOUNT_VERSION
    );

    mapper = createObjectMapperInstance();
  }

  @Test
  @DisplayName("should serialize address data")
  void shouldSerializeAddressData() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, JsonProcessingException {
    Data data = createDataInstance(DATA_ID, DATA_PUBLIC_ID, ENTRY, DataType.ADDRESS, account, DATA_CREATED_AT, DATA_UPDATED_AT, DATA_VERSION);
    DataDTO dataDTO = DataDTO.fromData(data);
    String json = mapper.writeValueAsString(dataDTO);

    ReadContext ctx = JsonPath.parse(json);

    assertThrows(PathNotFoundException.class, () -> ctx.read("$.id"));
    assertEquals(DATA_PUBLIC_ID.toString(), ctx.read("$.publicId"));
    assertEquals(ENTRY, ctx.read("$.entry"));
    assertEquals(DataType.ADDRESS.toString(), ctx.read("$.type"));
    assertEquals(convertInstantIntoString(DATA_CREATED_AT), ctx.read("$.createdAt"));
    assertEquals(convertInstantIntoString(DATA_UPDATED_AT), ctx.read("$.updatedAt"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.version"));
  }

  @Test
  @DisplayName("should serialize password data")
  void shouldSerializePasswordData() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, JsonProcessingException {
    Data data = createDataInstance(DATA_ID, DATA_PUBLIC_ID, ENTRY, DataType.PASSWORD, account, DATA_CREATED_AT, DATA_UPDATED_AT, DATA_VERSION);
    DataDTO dataDTO = DataDTO.fromData(data);
    String json = mapper.writeValueAsString(dataDTO);

    ReadContext ctx = JsonPath.parse(json);

    assertThrows(PathNotFoundException.class, () -> ctx.read("$.id"));
    assertEquals(DATA_PUBLIC_ID.toString(), ctx.read("$.publicId"));
    assertEquals(ENTRY, ctx.read("$.entry"));
    assertEquals(DataType.PASSWORD.toString(), ctx.read("$.type"));
    assertEquals(convertInstantIntoString(DATA_CREATED_AT), ctx.read("$.createdAt"));
    assertEquals(convertInstantIntoString(DATA_UPDATED_AT), ctx.read("$.updatedAt"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.version"));
  }

  @Test
  @DisplayName("should serialize site data")
  void shouldSerializeSiteData() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, JsonProcessingException {
    Data data = createDataInstance(DATA_ID, DATA_PUBLIC_ID, ENTRY, DataType.SITE, account, DATA_CREATED_AT, DATA_UPDATED_AT, DATA_VERSION);
    DataDTO dataDTO = DataDTO.fromData(data);
    String json = mapper.writeValueAsString(dataDTO);

    ReadContext ctx = JsonPath.parse(json);

    assertThrows(PathNotFoundException.class, () -> ctx.read("$.id"));
    assertEquals(DATA_PUBLIC_ID.toString(), ctx.read("$.publicId"));
    assertEquals(ENTRY, ctx.read("$.entry"));
    assertEquals(DataType.SITE.toString(), ctx.read("$.type"));
    assertEquals(convertInstantIntoString(DATA_CREATED_AT), ctx.read("$.createdAt"));
    assertEquals(convertInstantIntoString(DATA_UPDATED_AT), ctx.read("$.updatedAt"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.version"));
  }

  @Test
  @DisplayName("should serialize note data")
  void shouldSerializeNoteData() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, JsonProcessingException {
    Data data = createDataInstance(DATA_ID, DATA_PUBLIC_ID, ENTRY, DataType.NOTE, account, DATA_CREATED_AT, DATA_UPDATED_AT, DATA_VERSION);
    DataDTO dataDTO = DataDTO.fromData(data);
    String json = mapper.writeValueAsString(dataDTO);

    ReadContext ctx = JsonPath.parse(json);

    assertThrows(PathNotFoundException.class, () -> ctx.read("$.id"));
    assertEquals(DATA_PUBLIC_ID.toString(), ctx.read("$.publicId"));
    assertEquals(ENTRY, ctx.read("$.entry"));
    assertEquals(DataType.NOTE.toString(), ctx.read("$.type"));
    assertEquals(convertInstantIntoString(DATA_CREATED_AT), ctx.read("$.createdAt"));
    assertEquals(convertInstantIntoString(DATA_UPDATED_AT), ctx.read("$.updatedAt"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.version"));
  }
}
