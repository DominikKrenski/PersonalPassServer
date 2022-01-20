package org.dominik.pass.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.dominik.pass.data.enums.DataType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Schema(description = "Class used to save and update data")
@Getter
@ToString
public final class EncryptedDataDTO {

  @Schema(
    description = "Type of the data",
    name = "type",
    required = true
  )
  @NotNull(message = "{data.null.message}", groups = DataCreate.class)
  private DataType type;

  @Schema(
    description = "Encoded message",
    name = "entry",
    required = true,
    type = "string",
    pattern = "^[a-fA-F0-9]{2,}$"
  )
  @NotBlank(message = "{data.blank.message}", groups = {DataCreate.class, DataUpdate.class})
  @Pattern(
      regexp = "^[a-fA-F0-9]{24}\\.[a-fA-F0-9]+$",
      message = "{data.pattern.message}",
      groups = {DataCreate.class, DataUpdate.class}
  )
  private String entry;

  public interface DataCreate {}
  public interface DataUpdate {}
}
