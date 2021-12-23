package org.dominik.pass.db.repositories;

import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.db.entities.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataRepository extends JpaRepository<Data, Long> {
  List<Data> findAllByTypeAndAccountPublicId(DataType type, UUID accountPublicId);
  List<Data> findAllByAccountPublicId(UUID accountPublicId);
  Optional<Data> findByPublicId(UUID publicId);
  long deleteByAccountPublicId(UUID accountPublicId);

  @Modifying
  @Query("UPDATE Data d SET d.entry = :entry, d.updatedAt = current_timestamp WHERE d.publicId = :publicId")
  int updateData(@Param("entry") String entry, @Param("publicId") UUID publicId);

  @Modifying
  @Query("DELETE FROM Data d WHERE d.publicId = :publicId")
  int deleteData(@Param("publicId") UUID publicId);
}
