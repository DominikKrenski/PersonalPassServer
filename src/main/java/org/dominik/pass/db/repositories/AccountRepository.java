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
  @Query("UPDATE Account a SET a.email = :newEmail WHERE a.email = :oldEmail")
  int updateEmail(@Param("newEmail") String newEmail, @Param("oldEmail") String oldEmail);

  @Modifying
  @Query("UPDATE Account a SET a.reminder = :reminder WHERE a.email = :email")
  int updateReminder(@Param("reminder") String reminder, @Param("email") String email);
}
