package org.dominik.pass.db.entities;

import lombok.*;
import org.dominik.pass.data.enums.DataType;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "data")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(callSuper = true)
public final class Data extends BaseEntity implements Serializable {
  @Serial private static final long serialVersionUID = 5L;

  @Id
  @SequenceGenerator(
      name = "data_id_seq_gen",
      sequenceName = "data_id_seq",
      allocationSize = 1
  )
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "data_id_seq_gen"
  )
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "public_id", updatable = false, columnDefinition = "UUID NOT NULL UNIQUE DEFAULT extensions.uuid_generate_v4()")
  private UUID publicId;

  @Column(name = "entry", columnDefinition = "TEXT NOT NULL")
  private String entry;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", updatable = false, columnDefinition = "VARCHAR(10) NOT NULL")
  private DataType type;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false
  )
  @JoinColumn(
      name = "account_id",
      nullable = false,
      foreignKey = @ForeignKey(
          name = "data_accounts_fk",
          foreignKeyDefinition = "ON DELETE CASCADE ON UPDATE CASCADE"
      )
  )
  @ToString.Exclude
  private Account account;

  public Data(@NonNull String entry, @NonNull DataType type, @NonNull Account account) {
    this.publicId = UUID.randomUUID();
    this.entry = entry;
    this.type = type;
    this.account = account;
  }
}
