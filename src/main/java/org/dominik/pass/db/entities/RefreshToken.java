package org.dominik.pass.db.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public final class RefreshToken extends BaseEntity implements Serializable {
  @Serial private static final long serialVersionUID = 4L;

  @Id
  @SequenceGenerator(
      name = "refresh_tokens_seq_gen",
      sequenceName = "refresh_tokens_id_seq",
      allocationSize = 1
  )
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "refresh_tokens_seq_gen"
  )
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "token", nullable = false, unique = true, length = 500)
  private String token;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false
  )
  @JoinColumn(
      name = "account_id",
      nullable = false,
      foreignKey = @ForeignKey(
          name = "refresh_tokens_accounts_pk",
          foreignKeyDefinition = "ON DELETE CASCADE ON UPDATE CASCADE"
      )
  )
  @ToString.Exclude
  private Account account;

  public RefreshToken(@NonNull String token, @NonNull Account account) {
    this.token = token;
    this.account = account;
  }
}
