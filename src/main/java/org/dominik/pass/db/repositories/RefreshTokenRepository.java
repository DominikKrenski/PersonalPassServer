package org.dominik.pass.db.repositories;

import org.dominik.pass.db.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  @Modifying
  @Query("DELETE FROM RefreshToken t WHERE t.account = (SELECT a FROM Account a WHERE a.publicId = :publicId)")
  int deleteAllAccountTokens(@Param("publicId") UUID publicId);

  List<RefreshToken> findByAccountPublicId(UUID publicId);
}
