package org.dominik.pass.db.repositories;

import org.dominik.pass.db.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {
  boolean existsByEmail(String email);
  Optional<Account> findByEmail(String email);
  Optional<Account> findByPublicId(UUID publicId);
}
