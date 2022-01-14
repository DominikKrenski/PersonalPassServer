package org.dominik.pass.services.implementations;

import lombok.NonNull;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Key;
import org.dominik.pass.db.repositories.KeyRepository;
import org.dominik.pass.errors.exceptions.InternalException;
import org.dominik.pass.services.definitions.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class KeyServiceImpl implements KeyService {
  private final KeyRepository keyRepository;

  @Autowired
  public KeyServiceImpl(KeyRepository keyRepository) {
    this.keyRepository = keyRepository;
  }

  @Override
  @Transactional
  public void deleteAccountKey(@NonNull UUID accountPublicId) {
    int deleted = keyRepository.deleteAccountKey(accountPublicId);

    if (deleted != 1)
      throw new InternalException("Key could not be deleted");
  }

  @Override
  @Transactional
  public void save(String key, Account account) {
    keyRepository.save(new Key(key, account));
  }

}
