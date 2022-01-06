package org.dominik.pass.db.repositories;

import org.dominik.pass.db.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {
  boolean existsByEmail(String email);
  Optional<Account> findByEmail(String email);
  Optional<Account> findByPublicId(UUID publicId);

  @Modifying
  @Query("UPDATE Account a SET a.email = :newEmail, a.updatedAt = current_timestamp WHERE a.email = :oldEmail")
  int updateEmail(@Param("newEmail") String newEmail, @Param("oldEmail") String oldEmail);

  @Modifying
  @Query("UPDATE Account a SET a.password = :password, a.salt = :salt WHERE a.publicId = :publicId")
  int updatePassword(
    @Param("publicId") UUID publicId,
    @Param("password") String password,
    @Param("salt") String salt
    );

  @Modifying
  @Query("DELETE Account a WHERE a.publicId = :publicId")
  int deleteAccount(@Param("publicId") UUID publicId);
}
