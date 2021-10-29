package org.dominik.pass.data.dto;

import lombok.*;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public final class AccountDTO implements Serializable {
  @Serial private static final long serialVersionUID = 4L;

  @NonNull private final Long id;
  @EqualsAndHashCode.Include @NonNull private final UUID publicId;
  @NonNull private final String email;
  @NonNull private final String password;
  @NonNull private final String salt;
  private final String reminder;
  @NonNull private final Role role;
  private final boolean accountNonExpired;
  private final boolean accountNonLocked;
  private final boolean credentialsNonExpired;
  private final boolean enabled;
  @NonNull private final Instant createdAt;
  @NonNull private final Instant updatedAt;
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
