package org.dominik.pass.security;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountDetailsServiceTest {
  private static final Long ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_ADMIN;
  private static final Instant CREATED_AT = Instant.now().minusSeconds(3000);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(1290);

  @Mock AccountService accountService;
  @InjectMocks AccountDetailsService accountDetailsService;

  @Test
  @DisplayName("should return UserDetails if account with given email exists")
  void shouldReturnUserDetailsIfAccountWithGivenEmailExists() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        true,
        false,
        true,
        false,
        CREATED_AT,
        UPDATED_AT,
        (short) 1
    );

    when(accountService.findByEmail(any(String.class))).thenReturn(AccountDTO.fromAccount(account));

    AccountDetails details = accountDetailsService.loadUserByUsername(EMAIL);

    assertEquals(EMAIL, details.getUsername());
    assertEquals(PUBLIC_ID.toString(), details.getPublicId().toString());
    assertEquals(PASSWORD, details.getPassword());
    assertTrue(details.isAccountNonExpired());
    assertFalse(details.isAccountNonLocked());
    assertTrue(details.isCredentialsNonExpired());
    assertFalse(details.isEnabled());
    assertTrue(details.getAuthorities().contains(new SimpleGrantedAuthority(ROLE.toString())));
  }

  @Test
  @DisplayName("should throw UsernameNotFoundException if account with given email does not exist")
  void shouldThrowUsernameNotFoundException() {
    when(accountService.findByEmail(any(String.class))).thenThrow(new NotFoundException("Account does not exist"));

    assertThrows(UsernameNotFoundException.class, () -> accountDetailsService.loadUserByUsername(EMAIL));
  }
}
