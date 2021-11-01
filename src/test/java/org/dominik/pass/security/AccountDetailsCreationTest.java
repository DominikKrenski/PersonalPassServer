package org.dominik.pass.security;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.*;

public class AccountDetailsCreationTest {

  @Test
  @DisplayName("should create AccountDetails instance")
  void shouldCreateAccountDetailsInstance() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Long id = 1L;
    UUID publicId = UUID.randomUUID();
    String email = "dominik.krenski@gmail.com";
    String password = "Dominik1984";
    String salt = "salt";
    String reminder = "dummy reminder";
    Role role = Role.ROLE_USER;
    Instant createdAt = Instant.now().minusSeconds(2000);
    Instant updatedAt = createdAt.plusSeconds(500);


    Account account = createAccountInstance(
        id,
        publicId,
        email,
        password,
        salt,
        reminder,
        role,
        true,
        false,
        true,
        false,
        createdAt,
        updatedAt,
        (short) 1
    );

    AccountDTO accountDTO = AccountDTO.fromAccount(account);
    AccountDetails accountDetails = AccountDetails.fromDTO(accountDTO);

    assertEquals(email, accountDetails.getUsername());
    assertEquals(publicId.toString(), accountDetails.getPublicId().toString());
    assertEquals(password, accountDetails.getPassword());
    assertTrue(accountDetails.isAccountNonExpired());
    assertFalse(accountDetails.isAccountNonLocked());
    assertTrue(accountDetails.isCredentialsNonExpired());
    assertFalse(accountDetails.isEnabled());
    assertTrue(accountDetails.getAuthorities().contains(new SimpleGrantedAuthority(Role.ROLE_USER.toString())));
  }
}
