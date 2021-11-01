package org.dominik.pass.security;

import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsService implements UserDetailsService {
  private final AccountService accountService;

  @Autowired
  public AccountDetailsService(AccountService accountService) {
    this.accountService = accountService;
  }

  @Override
  public AccountDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    try {
      return AccountDetails.fromDTO(accountService.findByEmail(email));
    } catch (NotFoundException ex) {
      throw new UsernameNotFoundException(ex.getMessage());
    }
  }
}
