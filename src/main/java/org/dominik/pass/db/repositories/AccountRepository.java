package org.dominik.pass.db.repositories;

import org.dominik.pass.db.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
