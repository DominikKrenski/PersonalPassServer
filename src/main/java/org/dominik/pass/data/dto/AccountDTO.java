package org.dominik.pass.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Class used to pass info about account to the client")
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@JsonIgnoreProperties({
    "id",
    "publicId",
    "password",
    "salt",
    "role",
    "accountNonExpired",
    "accountNonLocked",
    "credentialsNonExpired",
    "enabled",
    "version"
})
public final class AccountDTO implements Serializable {
  @Serial private static final long serialVersionUID = 4L;

  @NonNull
  private final Long id;

  @EqualsAndHashCode.Include
  @NonNull
  private final UUID publicId;

  @Schema(
    description = "User's email address",
    name = "email",
    type = "string",
    maxLength = 360,
    required = true
  )
  @NonNull
  private final String email;

  @NonNull
  private final String password;

  @NonNull
  private final String salt;

  @Schema(
    description = "Password reminder",
    name = "reminder",
    type = "string"
  )
  private final String reminder;

  @NonNull
  private final Role role;

  private final boolean accountNonExpired;
  private final boolean accountNonLocked;
  private final boolean credentialsNonExpired;
  private final boolean enabled;

  @Schema(
    description = "Account creation date",
    name = "createdAt",
    required = true,
    type = "string",
    example = "10/12/2021T05:10:35.111Z"
  )
  @NonNull
  private final Instant createdAt;

  @Schema(
    description = "Last account modification date",
    name = "updatedAt",
    required = true,
    type = "string",
    example = "10/12/2021T05:10:35.111Z"
  )
  @NonNull
  private final Instant updatedAt;

  private final short version;

  public static AccountDTO fromAccount(@NonNull Account account) {
    return new AccountDTO(
        account.getId(),
        account.getPublicId(),
        account.getEmail(),
        account.getPassword(),
        account.getSalt(),
        account.getReminder(),
        account.getRole(),
        account.isAccountNonExpired(),
        account.isAccountNonLocked(),
        account.isCredentialsNonExpired(),
        account.isEnabled(),
        account.getCreatedAt(),
        account.getUpdatedAt(),
        account.getVersion()
    );
  }
}
