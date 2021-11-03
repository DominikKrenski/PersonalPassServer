package org.dominik.pass.services.definitions;

public interface RefreshTokenService {
  void login(String refreshToken, String email);
}
