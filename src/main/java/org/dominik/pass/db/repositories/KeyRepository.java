package org.dominik.pass.db.repositories;

import org.dominik.pass.db.entities.Key;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface KeyRepository extends JpaRepository<Key, Long> {

  @Modifying
  @Query("DELETE FROM Key k WHERE k.account = (SELECT a FROM Account a WHERE a.publicId = :publicId)")
  int deleteAccountKey(@Param("publicId") UUID accountPublicId);
}
