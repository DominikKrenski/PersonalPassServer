package org.dominik.pass.services.definitions;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RegistrationDTO;

import java.util.UUID;

public interface AccountService {
  AccountDTO register(RegistrationDTO dto);
  AccountDTO findByEmail(String email);
  AccountDTO findByPublicId(UUID publicId);
  int updateEmail(String newEmail, String oldEmail);
  int updateReminder(String reminder, String email);
  boolean existsByEmail(String email);
}
