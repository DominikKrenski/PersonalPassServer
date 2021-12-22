package org.dominik.pass.db.entities;

import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.data.enums.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.*;

class DataCreationTest {
  private static final Long ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_ADMIN;
  private static final Instant CREATED_AT = Instant.now().minusSeconds(5000);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(2000);
  private static final short VERSION = 0;
  private static final String ENTRY = "entry";

  private static Account account;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        false,
        false,
        false,
        false,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );
  }

  @Test
  @DisplayName("should create instance of type address")
  void shouldCreateInstanceOfTypeAddress() {
    Data data = new Data(ENTRY, DataType.ADDRESS, account);

    assertNull(data.getId());
    assertNotNull(data.getPublicId());
    assertEquals(ENTRY, data.getEntry());
    assertEquals(DataType.ADDRESS, data.getType());
    assertNotNull(data.getAccount());
    assertNull(data.getCreatedAt());
    assertNull(data.getUpdatedAt());
    assertEquals(0, data.getVersion());
  }

  @Test
  @DisplayName("should create instance of type password")
  void shouldCreateInstanceOfTypePassword() {
    Data data = new Data(ENTRY, DataType.PASSWORD, account);

    assertNull(data.getId());
    assertNotNull(data.getPublicId());
    assertEquals(ENTRY, data.getEntry());
    assertEquals(DataType.PASSWORD, data.getType());
    assertNotNull(data.getAccount());
    assertNull(data.getCreatedAt());
    assertNull(data.getUpdatedAt());
    assertEquals(0, data.getVersion());
  }

  @Test
  @DisplayName("should create instance of type site")
  void shouldCreateInstanceOfTypeSite() {
    Data data = new Data(ENTRY, DataType.SITE, account);

    assertNull(data.getId());
    assertNotNull(data.getPublicId());
    assertEquals(ENTRY, data.getEntry());
    assertEquals(DataType.SITE, data.getType());
    assertNotNull(data.getAccount());
    assertNull(data.getCreatedAt());
    assertNull(data.getUpdatedAt());
    assertEquals(0, data.getVersion());
  }

  @Test
  @DisplayName("should create instance of type note")
  void shouldCreateInstanceOfTypeNote() {
    Data data = new Data(ENTRY, DataType.NOTE, account);

    assertNull(data.getId());
    assertNotNull(data.getPublicId());
    assertEquals(ENTRY, data.getEntry());
    assertEquals(DataType.NOTE, data.getType());
    assertNotNull(data.getAccount());
    assertNull(data.getCreatedAt());
    assertNull(data.getUpdatedAt());
    assertEquals(0, data.getVersion());
  }
}
