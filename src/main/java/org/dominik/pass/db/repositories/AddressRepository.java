package org.dominik.pass.db.repositories;

import org.dominik.pass.db.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findAllByAccountPublicId(UUID accountPublicId);
  Optional<Address> findByPublicId(UUID publicId);

  @Modifying
  @Query("UPDATE Address a SET a.address = :address, a.updatedAt = current_timestamp WHERE a.publicId = :publicId")
  int updateAddress(@Param("address") String address, @Param("publicId") UUID publicId);

  @Modifying
  @Query("DELETE FROM Address a WHERE a.publicId = :publicId")
  int deleteAddress(@Param("publicId") UUID publicId);
}
