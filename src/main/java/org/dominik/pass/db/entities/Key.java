package org.dominik.pass.db.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "keys")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public final class Key extends BaseEntity implements Serializable {
  @Serial private static final long serialVersionUID = 8L;

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "key", columnDefinition = "BPCHAR(32) NOT NULL")
  private String key;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(
    name = "id",
    nullable = false,
    foreignKey = @ForeignKey(
      name = "keys_accounts_pk",
      foreignKeyDefinition = "ON DELETE CASCADE ON UPDATE CASCADE"
    )
  )
  @ToString.Exclude
  private Account account;

  public Key(@NonNull String key, @NonNull Account account) {
    this.key = key;
    this.account = account;
  }
}
