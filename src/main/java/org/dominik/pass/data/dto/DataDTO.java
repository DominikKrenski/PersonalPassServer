package org.dominik.pass.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.db.entities.Data;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Class used to pass data to the client")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@ToString
@JsonIgnoreProperties({
    "id",
    "account",
    "version"
})
public final class DataDTO {
  @NonNull
  private final Long id;

  @Schema(
    description = "Data's public id",
    name = "publicId",
    required = true,
    type = "string($uuid)"
  )
  @NonNull
  @EqualsAndHashCode.Include
  private final UUID publicId;

  @Schema(
    description = "Encrypted data",
    name = "entry",
    required = true,
    type = "string",
    pattern = "^[a-fA-F0-9]{2,}$"
  )
  @NonNull
  private final String entry;

  @Schema(
    description = "Data type",
    name = "type",
    required = true
  )
  @NonNull
  private final DataType type;

  @NonNull
  private final AccountDTO account;

  @Schema(
    description = "Data creation date",
    name = "createdAt",
    required = true,
    example = "01/02/2019T20:09:12.345Z"
  )
  @NonNull
  private final Instant createdAt;

  @Schema(
    description = "Last data modification date",
    name = "updatedAt",
    required = true,
    example = "01/02/2019T20:09:12.345Z"
  )
  @NonNull
  private final Instant updatedAt;

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
