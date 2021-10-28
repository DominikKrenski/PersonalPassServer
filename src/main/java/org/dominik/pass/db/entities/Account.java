package org.dominik.pass.db.entities;

import lombok.*;
import org.dominik.pass.data.enums.Role;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
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
      @NonNull UUID publicId,
      @NonNull String email,
      @NonNull String password,
      @NonNull String salt,
      String reminder,
      @NonNull Role role,
      boolean accountNonExpired,
      boolean accountNonLocked,
      boolean credentialsNonExpired,
      boolean enabled
  ) {
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

  public static AccountBuilder builder() {
    return new AccountBuilder();
  }

  public static final class AccountBuilder {
    private UUID publicId;
    private String email;
    private String password;
    private String salt;
    private String reminder;
    private Role role;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    public AccountBuilder publicId(@NonNull UUID publicId) {
      this.publicId = publicId;
      return this;
    }

    public AccountBuilder email(@NonNull String email) {
      this.email = email;
      return this;
    }

    public AccountBuilder password(@NonNull String password) {
      this.password = password;
      return this;
    }

    public AccountBuilder salt(@NonNull String salt) {
      this.salt = salt;
      return this;
    }

    public AccountBuilder reminder(@NonNull String reminder) {
      this.reminder = reminder;
      return this;
    }

    public AccountBuilder role(@NonNull Role role) {
      this.role = role;
      return this;
    }

    public AccountBuilder accountNonExpired(boolean accountNonExpired) {
      this.accountNonExpired = accountNonExpired;
      return this;
    }

    public AccountBuilder accountNonLocked(boolean accountNonLocked) {
      this.accountNonLocked = accountNonLocked;
      return this;
    }

    public AccountBuilder credentialsNonExpired(boolean credentialsNonExpired) {
      this.credentialsNonExpired = credentialsNonExpired;
      return this;
    }

    public AccountBuilder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Account build() {
      if (publicId == null)
        publicId = UUID.randomUUID();

      if (email == null)
        throw new IllegalStateException("Email must not be null");

      if (password == null)
        throw new IllegalStateException("Password must not be null");

      if (salt == null)
        throw new IllegalStateException("Salt must not be null");

      if (role == null)
        role = Role.ROLE_USER;

      return new Account(
          publicId,
          email,
          password,
          salt,
          reminder,
          role,
          accountNonExpired,
          accountNonLocked,
          credentialsNonExpired,
          enabled
      );
    }
  }
}
