package org.dominik.pass.db.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "addresses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public final class Address extends BaseEntity implements Serializable {
  @Serial private static final long serialVersionUID = 5L;

  @Id
  @SequenceGenerator(
      name = "addresses_id_seq_gen",
      sequenceName = "addresses_id_seq",
      allocationSize = 1
  )
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "addresses_id_seq_gen"
  )
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "address", columnDefinition = "TEXT NOT NULL UNIQUE")
  private String address;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false
  )
  @JoinColumn(
      name = "account_id",
      nullable = false,
      foreignKey = @ForeignKey(
          name = "addresses_accounts_fk",
          foreignKeyDefinition = "ON DELETE CASCADE ON UPDATE CASCADE"
      )
  )
  @ToString.Exclude
  private Account account;

  public Address(@NonNull String address, @NonNull Account account) {
    this.address = address;
    this.account = account;
  }
}
