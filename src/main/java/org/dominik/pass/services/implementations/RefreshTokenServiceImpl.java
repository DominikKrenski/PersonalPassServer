package org.dominik.pass.services.implementations;

import lombok.NonNull;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.RefreshToken;
import org.dominik.pass.db.repositories.RefreshTokenRepository;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
}
