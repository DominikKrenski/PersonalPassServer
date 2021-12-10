package org.dominik.pass.db.repositories;

import org.dominik.pass.configuration.DataJpaTestConfiguration;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Address;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
    properties = {
        "spring.main.banner-mode=off"
    }
)
@Import(DataJpaTestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:sql/03.address-repository-test.sql")
@ActiveProfiles("integration")
class AddressRepositoryIT {
  @Autowired private TestEntityManager em;
  @Autowired private AccountRepository accountRepository;
  @Autowired private AddressRepository addressRepository;

  @Test
  @DisplayName("should find all addresses that belong to dominik")
  void shouldFindAllAddressesThatBelongToDominik() {
    Account account = accountRepository
        .findByEmail("dominik.krenski@gmail.com")
        .orElseThrow(() -> new NotFoundException("Account not found"));

    List<Address> addresses = addressRepository.findAllByAccountPublicId(account.getPublicId());

    assertEquals(3, addresses.size());
  }

  @Test
  @DisplayName("should find all addresses that belong to dorota")
  void shouldFildAllAddressesThatBelongToDorota() {
    Account account = accountRepository
        .findByEmail("dorciad@interia.pl")
        .orElseThrow(() -> new NotFoundException("Account not found"));

    List<Address> addresses = addressRepository.findAllByAccountPublicId(account.getPublicId());

    assertEquals(1, addresses.size());
  }

  @Test
  @DisplayName("should return empty list if account does not have any addresses")
  void shouldReturnEmptyListIfAccountDoesNotHaveAnyAddresses() {
    Account account = accountRepository
        .findByEmail("dominik@yahoo.com")
        .orElseThrow(() -> new NotFoundException("Account not found"));

    List<Address> addresses = addressRepository.findAllByAccountPublicId(account.getPublicId());

    assertEquals(0, addresses.size());
  }

  @Test
  @DisplayName("should find address by public id")
  void shouldFindAddressByPublicId() {
    List<Address> addresses = addressRepository.findAll();
    Address address =
        addressRepository
            .findByPublicId(addresses.get(0).getPublicId())
            .orElseThrow(() -> new NotFoundException("Address not found"));

    assertEquals(addresses.get(0).getId(), address.getId());
    assertEquals(addresses.get(0).getPublicId().toString(), address.getPublicId().toString());
    assertEquals(addresses.get(0).getAddress(), address.getAddress());
  }

  @Test
  @DisplayName("should not find address with given public id")
  void shouldNotFindAddressWithGivenPublicId() {
    Optional<Address> address = addressRepository.findByPublicId(UUID.randomUUID());
    assertTrue(address.isEmpty());
  }

  @Test
  @DisplayName("should update address with given public id")
  void shouldUpdateAddressWithGivenPublicId() {
    List<Address> addresses = addressRepository.findAll();
    Address address = addresses.get(2);

    int updated = addressRepository.updateAddress("new dummy address", address.getPublicId());
    assertEquals(1, updated);
  }

  @Test
  @DisplayName("should not update if address with given public id does not exist")
  void shouldNotUpdateIfAddressWithGivenPublicIdNotExists() {
    int updated = addressRepository.updateAddress("new address", UUID.randomUUID());

    assertEquals(0, updated);
  }

  @Test
  @DisplayName("should delete address with given public id")
  void shouldDeleteAddressWithGivenPublicId() {
    List<Address> addresses = addressRepository.findAll();

    int deleted = addressRepository.deleteAddress(addresses.get(3).getPublicId());

    assertEquals(1, deleted);
  }

  @Test
  @DisplayName("should not delete anything if address with given public id does not exist")
  void shouldNotDeleteIfAddressWithGivenPublicIdDoesNotExist() {
    int deleted = addressRepository.deleteAddress(UUID.randomUUID());

    assertEquals(0, deleted);
  }
}
