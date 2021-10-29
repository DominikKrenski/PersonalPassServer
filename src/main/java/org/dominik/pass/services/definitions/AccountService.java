package org.dominik.pass.services.definitions;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RegistrationDTO;

public interface AccountService {
  AccountDTO register(RegistrationDTO dto);
}
