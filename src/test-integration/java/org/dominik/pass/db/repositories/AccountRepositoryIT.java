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

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(DataJpaTestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:sql/02.auth-controller-test.sql")
@ActiveProfiles("integration")
class AccountRepositoryIT {

  @Autowired AccountRepository accountRepository;

  @Test
  @DisplayName("should not update email if account does not exist")
  void shouldNotUpdateEmailIfAccountNotExist() {
    int result = accountRepository.updateEmail("dorciad@interia.pl", "dominik@yahoo.com");

    assertEquals(0, result);
  }

  @Test
  @DisplayName("should update email")
  void shouldUpdateEmail() {
    int result = accountRepository.updateEmail("dominik.krenski@yahoo.com", "dominik.krenski@gmail.com");

    assertEquals(1, result);
  }
}
