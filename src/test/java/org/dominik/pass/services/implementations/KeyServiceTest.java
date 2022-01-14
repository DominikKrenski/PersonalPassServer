package org.dominik.pass.services.implementations;

import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Key;
import org.dominik.pass.db.repositories.KeyRepository;
import org.dominik.pass.errors.exceptions.InternalException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createKeyInstance;
import static org.dominik.pass.utils.TestUtils.prepareAccountList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyServiceTest {
  private static final Long ID = 1L;
  private static final String KEY = "7782FE5167E1008F726367335BC98224";
  private static final Instant CREATED_AT = Instant.now().minusSeconds(5000);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(2900);
  private static final short VERSION = 0;
  private static List<Account> accounts = new LinkedList<>();

  @Mock KeyRepository keyRepository;
  @InjectMocks KeyServiceImpl keyService;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    accounts = prepareAccountList();
  }

  @Test
  @DisplayName("should save new key")
  void shouldSaveNewKey() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Key key = createKeyInstance(ID, KEY, accounts.get(0), CREATED_AT, UPDATED_AT, VERSION);

    when(keyRepository.save(any(Key.class))).thenReturn(key);

    keyService.save(KEY, accounts.get(0));

    verify(keyRepository).save(any(Key.class));
  }

  @Test
  @DisplayName("should delete key")
  void shouldDeleteKey() {
    when(keyRepository.deleteAccountKey(any(UUID.class))).thenReturn(1);

    keyService.deleteAccountKey(UUID.randomUUID());

    verify(keyRepository).deleteAccountKey(any(UUID.class));
  }

  @Test
  @DisplayName("should throw exception if key does not exist")
  void shouldThrowExceptionIfKeyDoesNotExist() {
    when(keyRepository.deleteAccountKey(any(UUID.class))).thenReturn(0);

    assertThrows(InternalException.class, () -> keyService.deleteAccountKey(UUID.randomUUID()));
  }
}
