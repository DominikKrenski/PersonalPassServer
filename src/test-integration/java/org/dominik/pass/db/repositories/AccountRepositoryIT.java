package org.dominik.pass.db.repositories;

import org.dominik.pass.configuration.DataJpaTestConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(DataJpaTestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:sql/03.sample-data.sql")
@ActiveProfiles("integration")
class AccountRepositoryIT {

  @Autowired AccountRepository accountRepository;

  @Test
  @DisplayName("should not update email if account does not exist")
  void shouldNotUpdateEmailIfAccountNotExist() {
    int result = accountRepository.updateEmail("krenska.dorota@gmail.com", "dominik.krenski@poczta.interia.pl");

    assertEquals(0, result);
  }

  @Test
  @DisplayName("should update email")
  void shouldUpdateEmail() {
    int result = accountRepository.updateEmail("dominik.krenski@poczta.interia.pl", "dominik.krenski@gmail.com");

    assertEquals(1, result);
  }

  @Test
  @DisplayName("should not delete account if one does not exist")
  void shouldNotDeleteAccountIfOneDoesNotExist() {
    int result = accountRepository.deleteAccount(UUID.randomUUID());

    assertEquals(0, result);
  }

  @Test
  @DisplayName("should delete account")
  void shouldDeleteAccount() {
    int result = accountRepository.deleteAccount(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"));

    assertEquals(1, result);
  }

  @Test
  @DisplayName("should update password")
  void shouldUpdatePassword() {
    int updated = accountRepository.updatePassword(
      UUID.fromString("e455b70f-50c5-4a96-9386-58f6ab9ba24b"),
      "new password",
      "new salt"
      );

    assertEquals(1, updated);
  }

  @Test
  @DisplayName("should not update password if account does not exist")
  void shouldNotUpdatePasswordIfAccountDoesNotExist() {
    int updated = accountRepository.updatePassword(
      UUID.randomUUID(),
      "new password",
      "new salt"
    );

    assertEquals(0, updated);
  }
}
