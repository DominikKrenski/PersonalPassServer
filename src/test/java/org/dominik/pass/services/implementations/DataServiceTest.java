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

  @Test
  @DisplayName("should update password")
  void shouldUpdatePassword() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    var data1 = createUpdateDataDtoInstance(
      UUID.fromString("9a998cd6-71b1-442b-b663-16aad7b499f3"),
      "2849eca061b3b17b3ad0b095.09462cb2d42e13694fa5a3179f0a5c8e74dffaa4f3f7ffd7782643fcbe1d9f3056530da982b412aea37f94f34d71e3fdadc1a735b3b0adb906105e0f84ef73339c6bc829748474d41f491ebc7967f768cb65bc6f5b8a9cc92f43c3689d59f55afbe9a67c302f442454c4f694b36b8135cd4573f56996765ae1577fe02a8a14eb7f93eaf2b1d26efbde59a55cbdd78022566a2d44fd8d29c46dc023baa999087d1c995ddc4a1b156b74349f88b26101119f51df67da71ed6f55c37b129da046ff925aaa7df9f9bd712e62fad469e1f00aa37825395640eb6565bca85f11d2ad66b6fef5b4536acb4339005d53256060f31e627c544913b2b72e686777eea0e29863a9848df078b69fb23d8759bbed3bb8295ee03bd96c958e9cad8d1ea02614e3a625d1e83f78f453c06fdddb3c95a8225dcdcfab0934500f5a4dccd350870dfa41f8",
      DataType.ADDRESS,
      convertStringToInstant("04/01/2022T16:00:36.405Z"),
      convertStringToInstant("04/01/2022T16:00:36.405Z")
    );

    var data2 = createUpdateDataDtoInstance(
      UUID.fromString("1274bba6-cba9-4ce2-9461-e1b3b7e00cef"),
      "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
      DataType.SITE,
      convertStringToInstant("03/12/2021T10:45:23.123Z"),
      convertStringToInstant("05/12/2021T15:50:49.745Z")
    );

    var data3 = createUpdateDataDtoInstance(
      UUID.fromString("d0471244-2795-4e74-8ab8-8850f38e0935"),
      "fa08fb639096dbe4c7ee66f9.0ea202ed3ed7ba34a8f33b0609d15e1c32fd561761b8edb19d4f72410f180fb4cbc1d5988bf6a8a3a50195e4e95aeffb0665009b4cdce68479674447cb0aac18ecf1f31f9234b7fc713f22f9aa530c536150c9f97188f55a69b45c5fad1de39d0ed6bede501b3a8e9a496238a4380cd4ecf89eb0add513bac6f067a49b1919973d8fbc12a3c3e7200840d53a86428bbe68ec7af43933fdbb921613bb71a01f",
      DataType.PASSWORD,
      convertStringToInstant("01/01/2022T08:13:23.999Z"),
      convertStringToInstant("01/01/2022T08:13:23.999Z")
    );

    var updatePasswordDTO = createUpdatePasswordDtoInstance(
      "new password",
      "new salt"
    );

    updatePasswordDTO.getData().addAll(List.of(data1, data2, data3));

    doNothing().when(accountService).updatePassword(any(UUID.class), anyString(), anyString());
    when(dataRepository.updateData(anyString(), any(UUID.class))).thenReturn(1);

    dataService.updateAllData(UUID.randomUUID(), updatePasswordDTO);
  }

  @Test
  @DisplayName("should throw NotFoundException if account could not be updated")
  void shouldThrowNotFoundExceptionIfAccountCouldNotBeUpdated() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    var updatePasswordDTO = createUpdatePasswordDtoInstance(
      "new password",
      "new salt"
    );

    doThrow(new NotFoundException("Account does not exist")).when(accountService).updatePassword(any(UUID.class), anyString(), anyString());

    assertThrows(NotFoundException.class, () -> dataService.updateAllData(UUID.randomUUID(), updatePasswordDTO));
    verify(dataRepository, never()).updateData(anyString(), any(UUID.class));
  }

  @Test
  @DisplayName("should throw NotFoundException if some data could not be updated")
  void shouldThrowNotFoundExceptionIfSomeDataCouldNotBeUpdated() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    var data1 = createUpdateDataDtoInstance(
      UUID.fromString("9a998cd6-71b1-442b-b663-16aad7b499f3"),
      "2849eca061b3b17b3ad0b095.09462cb2d42e13694fa5a3179f0a5c8e74dffaa4f3f7ffd7782643fcbe1d9f3056530da982b412aea37f94f34d71e3fdadc1a735b3b0adb906105e0f84ef73339c6bc829748474d41f491ebc7967f768cb65bc6f5b8a9cc92f43c3689d59f55afbe9a67c302f442454c4f694b36b8135cd4573f56996765ae1577fe02a8a14eb7f93eaf2b1d26efbde59a55cbdd78022566a2d44fd8d29c46dc023baa999087d1c995ddc4a1b156b74349f88b26101119f51df67da71ed6f55c37b129da046ff925aaa7df9f9bd712e62fad469e1f00aa37825395640eb6565bca85f11d2ad66b6fef5b4536acb4339005d53256060f31e627c544913b2b72e686777eea0e29863a9848df078b69fb23d8759bbed3bb8295ee03bd96c958e9cad8d1ea02614e3a625d1e83f78f453c06fdddb3c95a8225dcdcfab0934500f5a4dccd350870dfa41f8",
      DataType.ADDRESS,
      convertStringToInstant("04/01/2022T16:00:36.405Z"),
      convertStringToInstant("04/01/2022T16:00:36.405Z")
    );

    var data2 = createUpdateDataDtoInstance(
      UUID.fromString("1274bba6-cba9-4ce2-9461-e1b3b7e00cef"),
      "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
      DataType.SITE,
      convertStringToInstant("03/12/2021T10:45:23.123Z"),
      convertStringToInstant("05/12/2021T15:50:49.745Z")
    );

    var data3 = createUpdateDataDtoInstance(
      UUID.fromString("d0471244-2795-4e74-8ab8-8850f38e0935"),
      "fa08fb639096dbe4c7ee66f9.0ea202ed3ed7ba34a8f33b0609d15e1c32fd561761b8edb19d4f72410f180fb4cbc1d5988bf6a8a3a50195e4e95aeffb0665009b4cdce68479674447cb0aac18ecf1f31f9234b7fc713f22f9aa530c536150c9f97188f55a69b45c5fad1de39d0ed6bede501b3a8e9a496238a4380cd4ecf89eb0add513bac6f067a49b1919973d8fbc12a3c3e7200840d53a86428bbe68ec7af43933fdbb921613bb71a01f",
      DataType.PASSWORD,
      convertStringToInstant("01/01/2022T08:13:23.999Z"),
      convertStringToInstant("01/01/2022T08:13:23.999Z")
    );

    var updatePasswordDTO = createUpdatePasswordDtoInstance(
      "new password",
      "new salt"
    );

    updatePasswordDTO.getData().addAll(List.of(data1, data2, data3));

    doNothing().when(accountService).updatePassword(any(UUID.class), anyString(), anyString());
    when(dataRepository.updateData(anyString(), any(UUID.class)))
      .thenReturn(1)
      .thenReturn(1)
      .thenReturn(0);

    assertThrows(NotFoundException.class, () -> dataService.updateAllData(UUID.randomUUID(), updatePasswordDTO));
  }

  @Test
  @DisplayName("should invoke only password update if data list is empty")
  void shouldInvokeOnlyPasswordUpdateIfDataListIsEmpty() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    var updatePasswordDTO = createUpdatePasswordDtoInstance(
      "new password",
      "new salt"
    );

    doNothing().when(accountService).updatePassword(any(UUID.class), anyString(), anyString());

    dataService.updateAllData(UUID.randomUUID(), updatePasswordDTO);

    verify(dataRepository, never()).updateData(anyString(), any(UUID.class));
  }
}
