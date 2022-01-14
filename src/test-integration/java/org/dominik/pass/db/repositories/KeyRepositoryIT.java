package org.dominik.pass.db.repositories;

import org.dominik.pass.configuration.DataJpaTestConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(
  properties = {
    "spring.main.banner-mode=off"
  }
)
@Import(DataJpaTestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:sql/03.sample-data.sql")
@ActiveProfiles("integration")
class KeyRepositoryIT {
  @Autowired KeyRepository keyRepository;
  @Autowired TestEntityManager em;

  @Test
  @DisplayName("should delete key belonging to dominik.krenski@gmail.com")
  void shouldDeleteKeyBelongingToDominikKrenski() {
    int count = keyRepository.deleteAccountKey(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"));

    assertEquals(1, count);
  }

  @Test
  @DisplayName("should not delete dorciad's key")
  void shouldNotDeleteDorciadKey() {
    int count = keyRepository.deleteAccountKey(UUID.fromString("e455b70f-50c5-4a96-9386-58f6ab9ba24b"));

    assertEquals(0, count);
  }
}
