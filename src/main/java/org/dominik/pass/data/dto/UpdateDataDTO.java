package org.dominik.pass.data.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.utils.deserializers.ApiInstantDeserializer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public final class UpdateDataDTO {

  @Schema(
    description = "Data's public id",
    name = "publicId",
    required = true
  )
  @NotNull(message="{public_id.null.message}")
  private UUID publicId;

  @Schema(
    description = "Encrypted data",
    name = "entry",
    required = true
  )
  @NotBlank(message = "{data.blank.message}")
  @Pattern(
    regexp = "^[a-fA-F0-9]{24}\\.[a-fA-F0-9]+$",
    message = "{data.pattern.message}"
  )
  private String entry;

  @Schema(
    description = "Data type",
    name = "type",
    required = true
  )
  @NotNull(message = "{data.null.message}")
  private DataType type;

  @Schema(
    description = "Data creation date",
    name = "createdAt",
    required = true,
    example = "05/12/2021T15:55:10.987Z"
  )
  @NotNull(message = "{timestamp.null.message}")
  @JsonDeserialize(using = ApiInstantDeserializer.class)
  private Instant createdAt;

  @Schema(
    description = "Last modified date",
    name = "updatedAt",
    required = true,
    example = "05/12/2021T15:55:10.987Z"
  )
  @NotNull(message = "{timestamp.null.message}")
  @JsonDeserialize(using = ApiInstantDeserializer.class)
  private Instant updatedAt;
}
