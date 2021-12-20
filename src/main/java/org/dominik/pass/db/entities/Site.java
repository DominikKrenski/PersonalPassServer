package org.dominik.pass.db.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "sites")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public final class Site extends BaseEntity implements Serializable {
  @Serial private static final long serialVersionUID = 6L;

  @Id
  @SequenceGenerator(
      name = "sites_id_seq_gen",
      sequenceName = "sites_id_seq",
      allocationSize = 1
  )
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "sites_id_seq_gen"
  )
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "public_id", updatable = false, columnDefinition = "UUID NOT NULL UNIQUE DEFAULT extensions.uuid_generate_v4()")
  private UUID publicId;

  @Column(name = "site", columnDefinition = "TEXT NOT NULL")
  private String site;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false
  )
  @JoinColumn(
      name = "account_id",
      nullable = false,
      foreignKey = @ForeignKey(
          name = "sites_accounts_fk",
          foreignKeyDefinition = "ON DELETE CASCADE ON UPDATE CASCADE"
      )
  )
  @ToString.Exclude
  private Account account;

  public Site(@NonNull String site, @NonNull Account account) {
    this.publicId = UUID.randomUUID();
    this.site = site;
    this.account = account;
  }
}
