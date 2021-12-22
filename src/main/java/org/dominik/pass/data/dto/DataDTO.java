package org.dominik.pass.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.db.entities.Data;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@ToString
@JsonIgnoreProperties({
    "id",
    "type",
    "account",
    "version"
})
public final class DataDTO {
  @NonNull private final Long id;
  @NonNull @EqualsAndHashCode.Include private final UUID publicId;
  @NonNull private final String entry;
  @NonNull private final DataType type;
  @NonNull private final AccountDTO account;
  @NonNull private final Instant createdAt;
  @NonNull private final Instant updatedAt;
  private final short version;

  public static DataDTO fromData(@NonNull Data data) {
    return new DataDTO(
        data.getId(),
        data.getPublicId(),
        data.getEntry(),
        data.getType(),
        AccountDTO.fromAccount(data.getAccount()),
        data.getCreatedAt(),
        data.getUpdatedAt(),
        data.getVersion()
    );
  }
}
