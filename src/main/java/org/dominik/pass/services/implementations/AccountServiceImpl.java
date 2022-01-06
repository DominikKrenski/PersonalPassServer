package org.dominik.pass.services.implementations;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.repositories.AccountRepository;
import org.dominik.pass.errors.exceptions.ConflictException;
import org.dominik.pass.errors.exceptions.InternalException;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.UUID;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  @PersistenceContext private EntityManager em;

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
  public AccountDTO updateEmail(String newEmail, String oldEmail) {
    if (existsByEmail(newEmail))
      throw new ConflictException("Email is already in use");

    int updatedRows = accountRepository.updateEmail(newEmail, oldEmail);

    if (updatedRows != 1)
      throw new InternalException("Email could not be updated");

    em.clear();

    return findByEmail(newEmail);
  }

  @Override
  public boolean existsByEmail(@NonNull String email) {
    return accountRepository.existsByEmail(email);
  }

  @Override
  @Transactional
  public void deleteAccount(@NonNull UUID publicId) {
    int deleted = accountRepository.deleteAccount(publicId);

    if (deleted != 1)
      throw new NotFoundException("Account with given id does not exist");
  }

  @Override
  @Transactional
  public void updatePassword(@NonNull UUID publicId, @NonNull String password, @NonNull String salt) {
    int updated = accountRepository.updatePassword(publicId, passwordEncoder.encode(password), salt);

    if (updated != 1)
      throw new NotFoundException("Account with with given id does not exist");
  }
}
