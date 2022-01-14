package org.dominik.pass.db.entities;

import org.dominik.pass.data.enums.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class KeyCreationTest {
  private static final Long ACCOUNT_ID = 1L;
  private static final UUID ACCOUNT_PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_USER;
  private static final Instant ACCOUNT_CREATED_AT = Instant.now().minusSeconds(5000);
  private static final Instant ACCOUNT_UPDATED_AT = Instant.now().minusSeconds(2000);
  private static final short ACCOUNT_VERSION = 0;
  private static final String KEY = "7782FE5167E1008F726367335BC98224";

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
      false,
      false,
      false,
      false,
      ACCOUNT_CREATED_AT,
      ACCOUNT_UPDATED_AT,
      ACCOUNT_VERSION
    );
  }

  @Test
  @DisplayName("should create instance")
  void shouldCreateInstance() {
    var key = new Key(KEY, account);

    assertNull(key.getId());
    assertEquals(KEY, key.getKey());
    assertNull(key.getCreatedAt());
    assertNull(key.getUpdatedAt());
    assertEquals(0, key.getVersion());
    assertNotNull(key.getAccount());
  }
}
