package org.dominik.pass.services.definitions;

import org.dominik.pass.data.dto.RefreshTokenDTO;

public interface RefreshTokenService {
  void login(String refreshToken, String email);
  RefreshTokenDTO findByToken(String token);
  int deleteAllAccountTokens(String publicId);
  void saveNewRefreshToken(String oldToken, String newToken, String publicId);
  void saveRefreshTokenAfterEmailUpdate(String newEmail, String oldEmail, String refreshToken);
}
