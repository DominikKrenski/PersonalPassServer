package org.dominik.pass.services.implementations;

import lombok.NonNull;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RefreshTokenDTO;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.RefreshToken;
import org.dominik.pass.db.repositories.RefreshTokenRepository;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private final AccountService accountService;
  private final RefreshTokenRepository tokenRepository;

  @PersistenceContext private EntityManager em;

  @Autowired
  public RefreshTokenServiceImpl(
      AccountService accountService,
      RefreshTokenRepository tokenRepository
  ) {
    this.accountService = accountService;
    this.tokenRepository = tokenRepository;
  }

  @Transactional
  @Override
  public void login(@NonNull String refreshToken, @NonNull String email) {
    // get account by email
    AccountDTO accountDTO = accountService.findByEmail(email);
    Account account = em.merge(Account.fromDTO(accountDTO));

    // delete all existing refresh tokens
    tokenRepository.deleteAllAccountTokens(account.getPublicId());

    // save new refresh token in database
    tokenRepository.save(new RefreshToken(refreshToken, account));
  }

  @Transactional
  @Override
  public void saveNewRefreshToken(@NonNull String oldToken, @NonNull String newToken, @NonNull String publicId) {
    // mark old refresh token as used
    tokenRepository.markTokenAsUsed(oldToken);

    // get related account
    AccountDTO accountDTO = accountService.findByPublicId(UUID.fromString(publicId));
    Account account = em.merge(Account.fromDTO(accountDTO));

    // save new token in database
    tokenRepository.save(new RefreshToken(newToken, account));
  }

  @Override
  public RefreshTokenDTO findByToken(@NonNull String token) {
    return tokenRepository
        .findByToken(token)
        .map(RefreshTokenDTO::fromRefreshTokenLazy)
        .orElseThrow(() -> new NotFoundException("Given token does not exist"));
  }

  @Transactional
  @Override
  public int deleteAllAccountTokens(@NonNull String publicId) {
    return tokenRepository.deleteAllAccountTokens(UUID.fromString(publicId));
  }
}
