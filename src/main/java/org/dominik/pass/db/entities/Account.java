package org.dominik.pass.db.entities;

import lombok.*;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.enums.Role;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public final class Account extends BaseEntity implements Serializable {
  @Serial
  private static final long serialVersionUID = 2L;

  @Id
  @SequenceGenerator(
      name = "accounts_seq_gen",
      sequenceName = "accounts_id_seq",
      allocationSize = 1
  )
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "accounts_seq_gen"
  )
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "public_id", updatable = false, columnDefinition = "UUID NOT NULL UNIQUE DEFAULT extensions.uuid_generate_v4()")
  private UUID publicId;

  @Column(name = "email", unique = true, nullable = false, length = 360)
  private String email;

  @Column(name = "password", nullable = false, length = 200)
  private String password;

  @Column(name = "salt", columnDefinition = "BPCHAR(32) NOT NULL")
  private String salt;

  @Column(name = "reminder")
  private String reminder;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", updatable = false, columnDefinition = "VARCHAR(10) NOT NULL DEFAULT 'ROLE_USER'")
  private Role role;

  @Column(name = "account_non_expired", columnDefinition = "BOOLEAN NOT NULL DEFAULT true")
  private boolean accountNonExpired;

  @Column(name = "account_non_locked", columnDefinition = "BOOLEAN NOT NULL DEFAULT true")
  private boolean accountNonLocked;

  @Column(name = "credentials_non_expired", columnDefinition = "BOOLEAN NOT NULL DEFAULT true")
  private boolean credentialsNonExpired;

  @Column(name = "enabled", columnDefinition = "BOOLEAN NOT NULL DEFAULT true")
  private boolean enabled;

  private Account(
      Long id,
      UUID publicId,
      String email,
      String password,
      String salt,
      String reminder,
      Role role,
      boolean accountNonExpired,
      boolean accountNonLocked,
      boolean credentialsNonExpired,
      boolean enabled,
      Instant createdAt,
      Instant updatedAt,
      short version
  ) {
    super(createdAt, updatedAt, version);

    this.id = id;
    this.publicId = publicId;
    this.email = email;
    this.password = password;
    this.salt = salt;
    this.reminder = reminder;
    this.role = role;
    this.accountNonExpired = accountNonExpired;
    this.accountNonLocked = accountNonLocked;
    this.credentialsNonExpired = credentialsNonExpired;
    this.enabled = enabled;
  }

  public Account(@NonNull String email, @NonNull String password, @NonNull String salt, String reminder) {
    this(
        null,
        UUID.randomUUID(),
        email,
        password,
        salt,
        reminder,
        Role.ROLE_USER,
        true,
        true,
        true,
        true,
        null,
        null,
        (short) 0
    );
  }

  public static Account fromDTO(AccountDTO dto) {
    return new Account(
        dto.getId(),
        dto.getPublicId(),
        dto.getEmail(),
        dto.getPassword(),
        dto.getSalt(),
        dto.getReminder(),
        dto.getRole(),
        dto.isAccountNonExpired(),
        dto.isAccountNonLocked(),
        dto.isCredentialsNonExpired(),
        dto.isEnabled(),
        dto.getCreatedAt(),
        dto.getUpdatedAt(),
        dto.getVersion()
    );
  }
}
