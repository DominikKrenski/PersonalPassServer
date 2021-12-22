package org.dominik.pass.data.dto;

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

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.dominik.pass.utils.TestUtils.createDataInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataDtoCreationTest {
  private static final Long ACCOUNT_ID = 1L;
  private static final Long DATA_ID = 2L;
  private static final UUID ACCOUNT_PUBLIC_ID = UUID.randomUUID();
  private static final UUID DATA_PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_USER;
  private static final Instant ACCOUNT_CREATED_AT = Instant.now().minusSeconds(3000);
  private static final Instant ACCOUNT_UPDATED_AT = Instant.now().minusSeconds(1500);
  private static final Instant DATA_CREATED_AT = Instant.now().minusSeconds(2000);
  private static final Instant DATA_UPDATED_AT = Instant.now().minusSeconds(400);
  private static final short ACCOUNT_VERSION = 1;
  private static final short DATA_VERSION = 0;
  private static final String ENTRY = "entry";

  private static Account account;

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
  }

  @Test
  @DisplayName("should create address dto instance")
  void shouldCreateAddressDtoInstance() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Data data = createDataInstance(DATA_ID, DATA_PUBLIC_ID, ENTRY, DataType.ADDRESS, account, DATA_CREATED_AT, DATA_UPDATED_AT, DATA_VERSION);

    DataDTO dto = DataDTO.fromData(data);

    assertEquals(DATA_ID, dto.getId());
    assertEquals(DATA_PUBLIC_ID, dto.getPublicId());
    assertEquals(ENTRY, dto.getEntry());
    assertEquals(DataType.ADDRESS, dto.getType());
    assertNotNull(dto.getAccount());
    assertEquals(DATA_CREATED_AT, dto.getCreatedAt());
    assertEquals(DATA_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(DATA_VERSION, dto.getVersion());
  }

  @Test
  @DisplayName("should create password dto instance")
  void shouldCreatePasswordDtoInstance() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Data data = createDataInstance(DATA_ID, DATA_PUBLIC_ID, ENTRY, DataType.PASSWORD, account, DATA_CREATED_AT, DATA_UPDATED_AT, DATA_VERSION);

    DataDTO dto = DataDTO.fromData(data);

    assertEquals(DATA_ID, dto.getId());
    assertEquals(DATA_PUBLIC_ID, dto.getPublicId());
    assertEquals(ENTRY, dto.getEntry());
    assertEquals(DataType.PASSWORD, dto.getType());
    assertNotNull(dto.getAccount());
    assertEquals(DATA_CREATED_AT, dto.getCreatedAt());
    assertEquals(DATA_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(DATA_VERSION, dto.getVersion());
  }

  @Test
  @DisplayName("should create site instance")
  void shouldCreateSiteInstance() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Data data = createDataInstance(DATA_ID, DATA_PUBLIC_ID, ENTRY, DataType.SITE, account, DATA_CREATED_AT, DATA_UPDATED_AT, DATA_VERSION);

    DataDTO dto = DataDTO.fromData(data);

    assertEquals(DATA_ID, dto.getId());
    assertEquals(DATA_PUBLIC_ID, dto.getPublicId());
    assertEquals(ENTRY, dto.getEntry());
    assertEquals(DataType.SITE, dto.getType());
    assertNotNull(dto.getAccount());
    assertEquals(DATA_CREATED_AT, dto.getCreatedAt());
    assertEquals(DATA_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(DATA_VERSION, dto.getVersion());
  }

  @Test
  @DisplayName("should create note instance")
  void shouldCreateNoteInstance() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Data data = createDataInstance(DATA_ID, DATA_PUBLIC_ID, ENTRY, DataType.NOTE, account, DATA_CREATED_AT, DATA_UPDATED_AT, DATA_VERSION);

    DataDTO dto = DataDTO.fromData(data);

    assertEquals(DATA_ID, dto.getId());
    assertEquals(DATA_PUBLIC_ID, dto.getPublicId());
    assertEquals(ENTRY, dto.getEntry());
    assertEquals(DataType.NOTE, dto.getType());
    assertNotNull(dto.getAccount());
    assertEquals(DATA_CREATED_AT, dto.getCreatedAt());
    assertEquals(DATA_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(DATA_VERSION, dto.getVersion());
  }
}
