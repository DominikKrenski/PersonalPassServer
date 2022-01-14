package org.dominik.pass.services.definitions;

import org.dominik.pass.db.entities.Account;

import java.util.UUID;

public interface KeyService {
  void save(String key, Account account);
  void deleteAccountKey(UUID accountPublicId);
}
