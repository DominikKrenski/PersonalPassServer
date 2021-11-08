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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter(AccessLevel.PRIVATE)
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

  public Account(@NonNull String email, @NonNull String password, @NonNull String salt, String reminder) {
    this.email = email;
    this.password = password;
    this.salt = salt;
    this.reminder = reminder;
    this.publicId = UUID.randomUUID();
    this.role = Role.ROLE_USER;
    this.accountNonExpired = true;
    this.accountNonLocked = true;
    this.credentialsNonExpired = true;
    this.enabled = true;
  }

  public static Account fromDTO(AccountDTO dto) {
    Account account = new Account();
    account.setId(dto.getId());
    account.setPublicId(dto.getPublicId());
    account.setEmail(dto.getEmail());
    account.setPassword(dto.getPassword());
    account.setSalt(dto.getSalt());
    account.setReminder(dto.getReminder());
    account.setRole(dto.getRole());
    account.setAccountNonExpired(dto.isAccountNonExpired());
    account.setAccountNonLocked(dto.isAccountNonLocked());
    account.setCredentialsNonExpired(dto.isCredentialsNonExpired());
    account.setEnabled(dto.isEnabled());
    account.setCreatedAt(dto.getCreatedAt());
    account.setUpdatedAt(dto.getUpdatedAt());
    account.setVersion(dto.getVersion());

    return account;
  }
}
