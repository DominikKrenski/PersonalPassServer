package org.dominik.pass.services.implementations;

import lombok.NonNull;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.repositories.AccountRepository;
import org.dominik.pass.errors.exceptions.ConflictException;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
    if (existsByEmail(dto.getEmail()))
      throw new ConflictException("Account with given email already exists");

    Account account = new Account(
        dto.getEmail(),
        passwordEncoder.encode(dto.getPassword()),
        dto.getSalt(),
        dto.getReminder()
    );

    return AccountDTO.fromAccount(accountRepository.save(account));
  }

  @Override
  public AccountDTO findByEmail(@NonNull String email) {
    return accountRepository
        .findByEmail(email)
        .map(AccountDTO::fromAccount)
        .orElseThrow(() -> new NotFoundException("Account does not exist"));
  }

  @Override
  public AccountDTO findByPublicId(@NonNull UUID publicId) {
    return accountRepository
        .findByPublicId(publicId)
        .map(AccountDTO::fromAccount)
        .orElseThrow(() -> new NotFoundException("Account does not exist"));
  }

  @Override
  @Transactional
  public int updateEmail(String newEmail, String oldEmail) {
    return accountRepository.updateEmail(newEmail, oldEmail);
  }

  @Override
  public boolean existsByEmail(@NonNull String email) {
    return accountRepository.existsByEmail(email);
  }
}
