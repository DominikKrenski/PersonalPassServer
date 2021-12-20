package org.dominik.pass.db.repositories;

import org.dominik.pass.db.entities.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, Long> {
  List<Site> findAllByAccountPublicId(UUID accountPublicId);
  Optional<Site> findByPublicId(UUID publicId);

  @Modifying
  @Query("UPDATE Site s SET s.site = :site, s.updatedAt = current_timestamp WHERE s.publicId = :publicId")
  int updateSite(@Param("site") String site, @Param("publicId") UUID publicId);

  @Modifying
  @Query("DELETE FROM Site s WHERE s.publicId = :publicId")
  int deleteSite(@Param("publicId") UUID publicId);
}
