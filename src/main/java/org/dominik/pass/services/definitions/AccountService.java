package org.dominik.pass.services.definitions;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RegistrationDTO;

import java.util.UUID;

public interface AccountService {
  AccountDTO register(RegistrationDTO dto);
  AccountDTO findByEmail(String email);
  AccountDTO findByPublicId(UUID publicId);
  AccountDTO updateEmail(String newEmail, String oldEmail);
  boolean existsByEmail(String email);
  void deleteAccount(UUID publicId);
  void updatePassword(UUID publicId, String password, String salt);
}
