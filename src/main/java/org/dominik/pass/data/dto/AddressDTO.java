package org.dominik.pass.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.dominik.pass.db.entities.Address;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@ToString
@JsonIgnoreProperties({
    "id",
    "account",
    "version"
})
public final class AddressDTO {
  @NonNull private final Long id;
  @NonNull@EqualsAndHashCode.Include private final UUID publicId;
  @NonNull private final String address;
  private final AccountDTO account;
  @NonNull private final Instant createdAt;
  @NonNull private final Instant updatedAt;
  private final short version;

  public static AddressDTO fromAddressLazy(@NonNull Address address) {
    return new AddressDTO(
        address.getId(),
        address.getPublicId(),
        address.getAddress(),
        null,
        address.getCreatedAt(),
        address.getUpdatedAt(),
        address.getVersion()
    );
  }

  public static AddressDTO fromAddressEager(@NonNull Address address) {
    return new AddressDTO(
        address.getId(),
        address.getPublicId(),
        address.getAddress(),
        AccountDTO.fromAccount(address.getAccount()),
        address.getCreatedAt(),
        address.getUpdatedAt(),
        address.getVersion()
    );
  }
}