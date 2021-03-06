package org.dominik.pass.db.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
abstract class BaseEntity {

  @CreatedDate
  @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp")
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp")
  private Instant updatedAt;

  @Version
  @Column(name = "version", columnDefinition = "SMALLINT NOT NULL DEFAULT 0")
  private short version;
}
