package org.dominik.pass.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.dominik.pass.utils.validators.Hex;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@NoArgsConstructor
@Getter
@ToString
public final class UpdatePasswordDTO {

  @Schema(
    description = "Encrypted password",
    name = "password",
    required = true,
    minLength = 64,
    maxLength = 64,
    pattern = "^[a-fA-F0-9]{2,}$"
  )
  @NotBlank(message = "{password.blank.message}")
  @Length(min = 64, max = 64, message = "{password.length.message}")
  @Hex(message = "{password.hex.message}")
  private String password;

  @Schema(
    description = "Salt used to hash password",
    name = "salt",
    required = true,
    minLength = 32,
    maxLength = 32,
    pattern = "^[a-fA-F0-9]{2,}$"
  )
  @NotBlank(message = "{salt.blank.message}")
  @Length(min = 32, max = 32, message = "{salt.length.message}")
  @Hex(message = "{salt.hex.message}")
  private String salt;

  @Schema(
    description = "Password reminder",
    name = "reminder",
    maxLength = 255,
    pattern = "^[a-fA-F0-9]{2,}$"
  )
  @Length(max = 255, message = "{reminder.length.message}")
  private String reminder;

  @Valid
  private List<UpdateDataDTO> data;
}
