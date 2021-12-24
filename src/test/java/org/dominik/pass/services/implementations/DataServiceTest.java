package org.dominik.pass.services.implementations;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.DataDTO;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Data;
import org.dominik.pass.db.repositories.DataRepository;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.junit.jupiter.api.AfterEach;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataServiceTest {
  private static List<Account> accounts = new LinkedList<>();
  private static List<Data> data = new LinkedList<>();

  @Mock
  private DataRepository dataRepository;
  @Mock
  private AccountService accountService;
  @Mock
  private EntityManager em;
  @InjectMocks
  private DataServiceImpl dataService;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    accounts = prepareAccountList();
    data = prepareDataList(accounts);
  }

  @AfterEach
  void cleanup() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    data = prepareDataList(accounts);
  }

  @Test
  @DisplayName("should save new address")
  void shouldSaveNewAddress() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    ReflectionTestUtils.setField(dataService, "em", em);

    Data obj = createDataInstance(
        16L,
        UUID.randomUUID(),
        "entry",
        DataType.ADDRESS,
        accounts.get(0),
        Instant.now(),
        Instant.now(),
        (short) 0
    );

    when(accountService.findByPublicId(any(UUID.class))).thenReturn(AccountDTO.fromAccount(accounts.get(0)));
    when(em.merge(any(Account.class))).thenReturn(accounts.get(0));
    when(dataRepository.save(any(Data.class))).thenAnswer(i -> {
      data.add(obj);
      return obj;
    });

    int initialSize = data.size();

    DataDTO dto = dataService.save("entry", DataType.ADDRESS, accounts.get(0).getPublicId());

    assertEquals(initialSize + 1, data.size());
    assertEquals(DataType.ADDRESS, dto.getType());
    assertEquals(accounts.get(0).getPublicId(), dto.getAccount().getPublicId());
  }

  @Test
  @DisplayName("should save new password")
  void shouldSaveNewPassword() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    ReflectionTestUtils.setField(dataService, "em", em);

    Data obj = createDataInstance(
        16L,
        UUID.randomUUID(),
        "entry",
        DataType.PASSWORD,
        accounts.get(1),
        Instant.now(),
        Instant.now(),
        (short) 0
    );

    when(accountService.findByPublicId(any(UUID.class))).thenReturn(AccountDTO.fromAccount(accounts.get(1)));
    when(em.merge(any(Account.class))).thenReturn(accounts.get(1));
    when(dataRepository.save(any(Data.class))).thenAnswer(i -> {
      data.add(obj);
      return obj;
    });

    int initialSize = data.size();

    DataDTO dto = dataService.save("entry", DataType.PASSWORD, accounts.get(1).getPublicId());

    assertEquals(initialSize + 1, data.size());
    assertEquals(DataType.PASSWORD, dto.getType());
    assertEquals(accounts.get(1).getPublicId(), dto.getAccount().getPublicId());
  }

  @Test
  @DisplayName("should save new site")
  void shouldSaveNewSite() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    ReflectionTestUtils.setField(dataService, "em", em);

    Data obj = createDataInstance(
        16L,
        UUID.randomUUID(),
        "entry",
        DataType.SITE,
        accounts.get(2),
        Instant.now(),
        Instant.now(),
        (short) 0
    );

    when(accountService.findByPublicId(any(UUID.class))).thenReturn(AccountDTO.fromAccount(accounts.get(2)));
    when(em.merge(any(Account.class))).thenReturn(accounts.get(2));
    when(dataRepository.save(any(Data.class))).thenAnswer(i -> {
      data.add(obj);
      return obj;
    });

    int initialSize = data.size();

    DataDTO dto = dataService.save("entry", DataType.SITE, accounts.get(2).getPublicId());

    assertEquals(initialSize + 1, data.size());
    assertEquals(DataType.SITE, dto.getType());
    assertEquals(accounts.get(2).getPublicId(), dto.getAccount().getPublicId());
  }

  @Test
  @DisplayName("should save new note")
  void shouldSaveNewNote() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    ReflectionTestUtils.setField(dataService, "em", em);

    Data obj = createDataInstance(
        16L,
        UUID.randomUUID(),
        "entry",
        DataType.NOTE,
        accounts.get(0),
        Instant.now(),
        Instant.now(),
        (short) 0
    );

    when(accountService.findByPublicId(any(UUID.class))).thenReturn(AccountDTO.fromAccount(accounts.get(0)));
    when(em.merge(any(Account.class))).thenReturn(accounts.get(0));
    when(dataRepository.save(any(Data.class))).thenAnswer(i -> {
      data.add(obj);
      return obj;
    });

    int initialSize = data.size();

    DataDTO dto = dataService.save("entry", DataType.NOTE, accounts.get(0).getPublicId());

    assertEquals(initialSize + 1, data.size());
    assertEquals(DataType.NOTE, dto.getType());
    assertEquals(accounts.get(0).getPublicId(), dto.getAccount().getPublicId());
  }

  @Test
  @DisplayName("should find all addresses belonging to dominik.krenski")
  void shouldFindAllAddressesBelongingToDominikKrenski() {
    when(
        dataRepository
            .findAllByTypeAndAccountPublicId(DataType.ADDRESS, accounts.get(0).getPublicId()))
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getType() == DataType.ADDRESS)
            .filter(d -> d.getAccount().getPublicId() == accounts.get(0).getPublicId())
            .toList());

    List<DataDTO> dtos = dataService.findAllUserDataByType(DataType.ADDRESS, accounts.get(0).getPublicId());

    assertEquals(2, dtos.size());
    assertTrue(dtos.containsAll(List.of(DataDTO.fromData(data.get(0)), DataDTO.fromData(data.get(1)))));
  }

  @Test
  @DisplayName("should find all passwords belonging to dominik.krenski")
  void shouldFindAllPasswordsBelongingToDominikKrenski() {
    when(
        dataRepository
            .findAllByTypeAndAccountPublicId(DataType.PASSWORD, accounts.get(0).getPublicId())
    )
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getType() == DataType.PASSWORD)
            .filter(d -> d.getAccount().getPublicId() == accounts.get(0).getPublicId())
            .toList());

    List<DataDTO> dtos = dataService.findAllUserDataByType(DataType.PASSWORD, accounts.get(0).getPublicId());

    assertTrue(dtos.containsAll(
        List.of(
            DataDTO.fromData(data.get(2)),
            DataDTO.fromData(data.get(3))
        )
    ));
  }

  @Test
  @DisplayName("should find all sites belonging to dominik.krenski")
  void shouldFindAllSitesBelongingToDominikKrenski() {
    when(
        dataRepository
            .findAllByTypeAndAccountPublicId(DataType.SITE, accounts.get(0).getPublicId())
    )
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getType() == DataType.SITE)
            .filter(d -> d.getAccount().getPublicId() == accounts.get(0).getPublicId())
            .toList());

    List<DataDTO> dtos = dataService.findAllUserDataByType(DataType.SITE, accounts.get(0).getPublicId());

    assertTrue(dtos.containsAll(
        List.of(
            DataDTO.fromData(data.get(4)),
            DataDTO.fromData(data.get(5))
        )
    ));
  }

  @Test
  @DisplayName("should find all notes belonging to dominik.krenski")
  void shouldFindAllNotesBelongingToDominikKrenski() {
    when(
        dataRepository
            .findAllByTypeAndAccountPublicId(DataType.NOTE, accounts.get(0).getPublicId())
    )
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getType() == DataType.NOTE)
            .filter(d -> d.getAccount().getPublicId() == accounts.get(0).getPublicId())
            .toList());

    List<DataDTO> dtos = dataService.findAllUserDataByType(DataType.NOTE, accounts.get(0).getPublicId());

    assertTrue(dtos.containsAll(
        List.of(
            DataDTO.fromData(data.get(6)),
            DataDTO.fromData(data.get(7))
        )
    ));
  }

  @Test
  @DisplayName("should return an empty list if user has no entries of given type")
  void shouldReuturnEmptyListIfUserHasHoEntriesOfGivenType() {
    when(
        dataRepository.findAllByTypeAndAccountPublicId(DataType.ADDRESS, accounts.get(2).getPublicId())
    )
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getType() == DataType.ADDRESS)
            .filter(d -> d.getAccount().getPublicId() == accounts.get(2).getPublicId())
            .toList());

    List<DataDTO> dtos = dataService.findAllUserDataByType(DataType.ADDRESS, accounts.get(2).getPublicId());

    assertEquals(0, dtos.size());
  }

  @Test
  @DisplayName("should find all data belonging to dominik.krenski")
  void shouldFindAllDataBelongingToDominikKrenski() {
    when(
        dataRepository.findAllByAccountPublicId(any(UUID.class))
    )
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getAccount().getPublicId() == accounts.get(0).getPublicId())
            .toList());

    List<DataDTO> dtos = dataService.findAllUserData(accounts.get(0).getPublicId());

    assertEquals(8, dtos.size());
  }

  @Test
  @DisplayName("should find all data belonging to dorciad")
  void shouldFindAllDataBelongingToDorciad() {
    when(
        dataRepository.findAllByAccountPublicId(UUID.fromString("e455b70f-50c5-4a96-9386-58f6ab9ba24b"))
    )
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getAccount().getPublicId() == accounts.get(1).getPublicId())
            .toList());

    List<DataDTO> dtos = dataService.findAllUserData(accounts.get(1).getPublicId());

    assertEquals(7, dtos.size());
  }

  @Test
  @DisplayName("should return an empty list when searching data belonging to dominik")
  void shouldReturnEmptyListWhenSearchingDataBelongingToDominik() {
    when(
        dataRepository.findAllByAccountPublicId(UUID.fromString("f01048b2-622a-49b6-963e-5e8edeec8026"))
    )
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getAccount().getPublicId() == accounts.get(2).getPublicId())
            .toList());

    List<DataDTO> dtos = dataService.findAllUserData(accounts.get(2).getPublicId());

    assertEquals(0, dtos.size());
  }

  @Test
  @DisplayName("should find data by public id")
  void shouldFindDataByPublicId() {
    when(
        dataRepository.findByPublicId(UUID.fromString("67f9c86f-36eb-4fab-94ac-f68d113ad9d7"))
    ).thenReturn(Optional.of(data.get(6)));

    DataDTO dto = dataService.findData(UUID.fromString("67f9c86f-36eb-4fab-94ac-f68d113ad9d7"));

    assertEquals(7, dto.getId());
    assertEquals("67f9c86f-36eb-4fab-94ac-f68d113ad9d7", dto.getPublicId().toString());
    assertEquals("entry_7", dto.getEntry());
    assertEquals(DataType.NOTE, dto.getType());
  }

  @Test
  @DisplayName("should throw NotFound if entry with public id does not exist")
  void shouldThrowNotFoundIfEntryWithPublicIdDoesNotExist() {
    when(dataRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> dataService.findData(UUID.randomUUID()));
  }

  @Test
  @DisplayName("should return all entries belonging to user")
  void shouldReturnAllEntriesBelongingToDominikKrenski() {
    when(dataRepository.deleteByAccountPublicId(any(UUID.class))).thenReturn(7L);

    dataService.deleteAllUserData(accounts.get(0).getPublicId());
    verify(dataRepository).deleteByAccountPublicId(any(UUID.class));
  }

  @Test
  @DisplayName("should delete entry by public id")
  void shouldDeleteEntryByPublicIc() {
    when(dataRepository.deleteData(any(UUID.class))).thenReturn(1);

    dataService.deleteData(UUID.randomUUID());
    verify(dataRepository).deleteData(any(UUID.class));
  }

  @Test
  @DisplayName("should throw NotFound if data was not deleted")
  void shouldThrowNotFoundExceptionIfDataWasNotDeleted() {
    when(dataRepository.deleteData(any(UUID.class))).thenReturn(0);

    assertThrows(NotFoundException.class, () -> dataService.deleteData(UUID.randomUUID()));

  }

  @Test
  @DisplayName("should update data")
  void shouldUpdateData() {
    when(dataRepository.updateData(anyString(), any(UUID.class))).thenReturn(1);
    dataService.updateData("new entry", UUID.randomUUID());

    verify(dataRepository).updateData(anyString(), any(UUID.class));
  }

  @Test
  @DisplayName("should throw NotFound if data was not updated")
  void shouldThrowNotFoundIfDataWasNotUpdated() {
    when(dataRepository.updateData(anyString(), any(UUID.class))).thenReturn(0);

    assertThrows(NotFoundException.class, () -> dataService.updateData("new entry", UUID.randomUUID()));
  }
}
