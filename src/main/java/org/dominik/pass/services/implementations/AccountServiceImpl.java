package org.dominik.pass.services.implementations;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.repositories.AccountRepository;
import org.dominik.pass.services.definitions.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public AccountDTO register(RegistrationDTO dto) {
    Account.AccountBuilder builder = Account
        .builder()
        .email(dto.getEmail())
        .password(passwordEncoder.encode(dto.getPassword()))
        .salt(dto.getSalt());

    if (dto.getReminder() != null)
      builder.reminder(dto.getReminder());

    return AccountDTO.fromAccount(accountRepository.save(builder.build()));
  }
}
