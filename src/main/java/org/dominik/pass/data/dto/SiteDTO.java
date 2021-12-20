package org.dominik.pass.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.dominik.pass.db.entities.Site;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@ToString
@JsonIgnoreProperties({
    "id",
    "account",
    "version"
})
public final class SiteDTO {
  @NonNull private final Long id;
  @NonNull @EqualsAndHashCode.Include private final UUID publicId;
  @NonNull private final String site;
  private final AccountDTO account;
  @NonNull private final Instant createdAt;
  @NonNull private final Instant updatedAt;
  private final short version;

  public static SiteDTO fromSiteLazy(@NonNull Site site) {
    return new SiteDTO(
        site.getId(),
        site.getPublicId(),
        site.getSite(),
        null,
        site.getCreatedAt(),
        site.getUpdatedAt(),
        site.getVersion()
    );
  }

  public static SiteDTO fromSiteEager(@NonNull Site site) {
    return new SiteDTO(
        site.getId(),
        site.getPublicId(),
        site.getSite(),
        AccountDTO.fromAccount(site.getAccount()),
        site.getCreatedAt(),
        site.getUpdatedAt(),
        site.getVersion()
    );
  }
}
