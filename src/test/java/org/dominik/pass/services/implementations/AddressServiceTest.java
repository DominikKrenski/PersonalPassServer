package org.dominik.pass.services.implementations;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.AddressDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Address;
import org.dominik.pass.db.repositories.AddressRepository;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.dominik.pass.utils.TestUtils.createAddressInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {
  private static final Long ID = 1L;
  private static final Long ADDRESS_ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final UUID ADDRESS_PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_ADMIN;
  private static final Instant CREATED_AT = Instant.now().minusSeconds(4000);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(2400);
  private static final Instant ADDRESS_CREATED_AT = Instant.now().minusSeconds(100);
  private static final Instant ADDRESS_UPDATED_AT = Instant.now().minusSeconds(100);
  private static final short VERSION = 0;
  private static final short ADDRESS_VERSION = 0;
  private static final String ENTRY = "address_1";

  private static Account account;
  private static Address address;

  @Mock private AddressRepository addressRepository;
  @Mock private AccountService accountService;
  @Mock private EntityManager em;
  @InjectMocks private AddressServiceImpl addressService;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        true,
        true,
        true,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    address = createAddressInstance(
        ADDRESS_ID,
        ADDRESS_PUBLIC_ID,
        ENTRY,
        account,
        ADDRESS_CREATED_AT,
        ADDRESS_UPDATED_AT,
        ADDRESS_VERSION
    );
  }

  @Test
  @DisplayName("should save new address")
  void shouldSaveNewAddress() {
    ReflectionTestUtils.setField(addressService, "em", em);

    when(accountService.findByPublicId(any(UUID.class))).thenReturn(AccountDTO.fromAccount(account));
    when(em.merge(any(Account.class))).thenReturn(account);
    when(addressRepository.save(any(Address.class))).thenReturn(address);

    AddressDTO dto = addressService.save("new address", ADDRESS_PUBLIC_ID.toString());

    assertEquals(ADDRESS_ID, dto.getId());
    assertEquals(ADDRESS_PUBLIC_ID.toString(), dto.getPublicId().toString());
    assertEquals(ENTRY, dto.getAddress());
    assertNull(dto.getAccount());
    assertEquals(ADDRESS_CREATED_AT, dto.getCreatedAt());
    assertEquals(ADDRESS_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(ADDRESS_VERSION, dto.getVersion());
  }

  @Test
  @DisplayName("should return all addresses belonging to user")
  void shouldReturnAllAddressesBelongingToUser() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    UUID addressPublicId = UUID.randomUUID();
    String entry = "second entry";
    Instant createdAt = Instant.now().minusSeconds(20);
    Instant updatedAt = createdAt.minusSeconds(10);
    short version = 1;

    Address address2 = createAddressInstance(2L, addressPublicId, entry, account, createdAt, updatedAt, version);

    when(addressRepository.findAllByAccountPublicId(any(UUID.class))).thenReturn(List.of(address, address2));

    List<AddressDTO> addresses = addressService.getAllUserAddresses(UUID.randomUUID());

    assertEquals(2, addresses.size());
  }

  @Test
  @DisplayName("should return empty list if user has no addresses")
  void shouldReturnEmptyListIfUserHasNoAddresses() {
    when(addressRepository.findAllByAccountPublicId(any(UUID.class))).thenReturn(new ArrayList<>());

    List<AddressDTO> addresses = addressService.getAllUserAddresses(UUID.randomUUID());

    assertEquals(0, addresses.size());
  }

  @Test
  @DisplayName("should return address by public id")
  void shouldReturnAddressByPublicId() {
    when(addressRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(address));

    AddressDTO dto = addressService.getAddress(UUID.randomUUID());

    assertEquals(ADDRESS_ID, dto.getId());
    assertEquals(ADDRESS_PUBLIC_ID, dto.getPublicId());
    assertEquals(ENTRY, dto.getAddress());
    assertNull(dto.getAccount());
    assertEquals(ADDRESS_CREATED_AT, dto.getCreatedAt());
    assertEquals(ADDRESS_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(ADDRESS_VERSION, dto.getVersion());
  }

  @Test
  @DisplayName("should throw NotFound if address with given public id does not exist")
  void shouldThrowNotFoundIfAddressWithPublicIdNotExists() {
    when(addressRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> addressService.getAddress(UUID.randomUUID()));
  }

  @Test
  @DisplayName("should update address")
  void shouldUpdateAddress() {
    when(addressRepository.updateAddress(anyString(), any(UUID.class))).thenReturn(1);

    int updated = addressService.updateAddress("address", UUID.randomUUID());

    assertEquals(1, updated);
  }

  @Test
  @DisplayName("should not update address")
  void shouldNotUpdateAddress() {
    when(addressRepository.updateAddress(anyString(), any(UUID.class))).thenReturn(0);

    int updated = addressService.updateAddress("address", UUID.randomUUID());

    assertEquals(0, updated);
  }

  @Test
  @DisplayName("should delete address")
  void shouldDeleteAddress() {
    when(addressRepository.deleteAddress(any(UUID.class))).thenReturn(1);

    int deleted = addressService.deleteAddress(UUID.randomUUID());

    assertEquals(1, deleted);
  }

  @Test
  @DisplayName("should not delete address")
  void shouldNotDeleteAddress() {
    when(addressRepository.deleteAddress(any(UUID.class))).thenReturn(0);

    int deleted = addressService.deleteAddress(UUID.randomUUID());

    assertEquals(0, deleted);
  }
}
