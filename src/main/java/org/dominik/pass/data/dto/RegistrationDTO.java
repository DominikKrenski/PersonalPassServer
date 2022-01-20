package org.dominik.pass.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.dominik.pass.utils.validators.EmailAddress;
import org.dominik.pass.utils.validators.Hex;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

@Getter
@ToString
public final class RegistrationDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @Schema(
    description = "User's email address",
    name = "email",
    type = "string",
    maxLength = 360
  )
  @NotBlank(message = "{email.blank.message}")
  @Length(max = 360, message = "{email.length.message}")
  @EmailAddress(message = "{email.format.message}")
  private String email;

  @Schema(
    description = "User's password",
    name = "password",
    type = "string",
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
    type = "string",
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
    type = "string",
    maxLength = 255
  )
  @Length(max = 255, message = "{reminder.length.message}")
  private String reminder;
}
