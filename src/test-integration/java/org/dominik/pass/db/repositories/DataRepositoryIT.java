package org.dominik.pass.db.repositories;

import org.dominik.pass.configuration.DataJpaTestConfiguration;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.db.entities.Data;
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

import java.util.Comparator;
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
@Sql("classpath:sql/03.sample-data.sql")
@ActiveProfiles("integration")
class DataRepositoryIT {
  @Autowired
  private DataRepository dataRepository;

  @Autowired
  private TestEntityManager em;

  @Test
  @DisplayName("should find all addresses that belong to dominik.krenski")
  void shouldFindAllAddressesThatBelongToDominikKrenski() {
    List<Data> data =
        dataRepository
            .findAllByTypeAndAccountPublicId(DataType.ADDRESS, UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"))
            .stream()
            .sorted(Comparator.comparing(Data::getEntry))
            .toList();

    assertEquals(2, data.size());

    assertNotNull(data.get(0).getId());
    assertEquals(UUID.fromString("84ab5b68-2fa4-44eb-bd49-c5ab44eac6cd"), data.get(0).getPublicId());
    assertEquals("entry_1", data.get(0).getEntry());
    assertEquals(DataType.ADDRESS, data.get(0).getType());
    assertEquals(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"), data.get(0).getAccount().getPublicId());
    assertNotNull(data.get(0).getCreatedAt());
    assertNotNull(data.get(0).getUpdatedAt());
    assertEquals(0, data.get(0).getVersion());

    assertNotNull(data.get(1).getId());
    assertEquals(UUID.fromString("ec28a035-a31b-461d-9fcd-70c9982c1a22"), data.get(1).getPublicId());
    assertEquals("entry_2", data.get(1).getEntry());
    assertEquals(DataType.ADDRESS, data.get(1).getType());
    assertEquals(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"), data.get(1).getAccount().getPublicId());
    assertNotNull(data.get(1).getCreatedAt());
    assertNotNull(data.get(1).getUpdatedAt());
    assertEquals(0, data.get(1).getVersion());
  }

  @Test
  @DisplayName("should find all passwords that belong to dominik.krenski")
  void shouldFindAllPasswordsThatBelongToDominikKrenski() {
    List<Data> data =
        dataRepository
            .findAllByTypeAndAccountPublicId(DataType.PASSWORD, UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"))
            .stream()
            .sorted(Comparator.comparing(Data::getEntry))
            .toList();

    assertEquals(2, data.size());

    assertNotNull(data.get(0).getId());
    assertEquals(UUID.fromString("9f569e10-64e1-4493-99e0-6a988a232e6b"), data.get(0).getPublicId());
    assertEquals("entry_3", data.get(0).getEntry());
    assertEquals(DataType.PASSWORD, data.get(0).getType());
    assertEquals(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"), data.get(0).getAccount().getPublicId());
    assertNotNull(data.get(0).getCreatedAt());
    assertNotNull(data.get(0).getUpdatedAt());
    assertEquals(0, data.get(0).getVersion());

    assertNotNull(data.get(1).getId());
    assertEquals(UUID.fromString("9bfc99d8-8bf3-45e4-b8ee-4c286408ac29"), data.get(1).getPublicId());
    assertEquals("entry_4", data.get(1).getEntry());
    assertEquals(DataType.PASSWORD, data.get(1).getType());
    assertEquals(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"), data.get(1).getAccount().getPublicId());
    assertNotNull(data.get(1).getCreatedAt());
    assertNotNull(data.get(1).getUpdatedAt());
    assertEquals(0, data.get(1).getVersion());
  }

  @Test
  @DisplayName("should find all sites that belong to dominik.krenski")
  void shouldFindAllSitesThatBelongToDominikKrenski() {
    List<Data> data =
        dataRepository
            .findAllByTypeAndAccountPublicId(DataType.SITE, UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"))
            .stream()
            .sorted(Comparator.comparing(Data::getEntry))
            .toList();

    assertEquals(2, data.size());

    assertNotNull(data.get(0).getId());
    assertEquals(UUID.fromString("05618eec-dc25-4c24-b908-4fce6cb04ad4"), data.get(0).getPublicId());
    assertEquals("entry_5", data.get(0).getEntry());
    assertEquals(DataType.SITE, data.get(0).getType());
    assertEquals(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"), data.get(0).getAccount().getPublicId());
    assertNotNull(data.get(0).getCreatedAt());
    assertNotNull(data.get(0).getUpdatedAt());
    assertEquals(0, data.get(0).getVersion());

    assertNotNull(data.get(1).getId());
    assertEquals(UUID.fromString("3299f2fe-f930-44b6-8b10-c23c2efe5d1f"), data.get(1).getPublicId());
    assertEquals("entry_6", data.get(1).getEntry());
    assertEquals(DataType.SITE, data.get(1).getType());
    assertEquals(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"), data.get(1).getAccount().getPublicId());
    assertNotNull(data.get(1).getCreatedAt());
    assertNotNull(data.get(1).getUpdatedAt());
    assertEquals(0, data.get(1).getVersion());
  }

  @Test
  @DisplayName("should find all notes that belong to dominik.krenski")
  void shouldFindAllNotesThatBelongToDominikKrenski() {
    List<Data> data =
        dataRepository
            .findAllByTypeAndAccountPublicId(DataType.NOTE, UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"))
            .stream()
            .sorted(Comparator.comparing(Data::getEntry))
            .toList();

    assertEquals(2, data.size());

    assertNotNull(data.get(0).getId());
    assertEquals(UUID.fromString("67f9c86f-36eb-4fab-94ac-f68d113ad9d7"), data.get(0).getPublicId());
    assertEquals("entry_7", data.get(0).getEntry());
    assertEquals(DataType.NOTE, data.get(0).getType());
    assertEquals(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"), data.get(0).getAccount().getPublicId());
    assertNotNull(data.get(0).getCreatedAt());
    assertNotNull(data.get(0).getUpdatedAt());
    assertEquals(0, data.get(0).getVersion());

    assertNotNull(data.get(1).getId());
    assertEquals(UUID.fromString("67a09bd7-5d26-4532-9758-9be4fa5a58c6"), data.get(1).getPublicId());
    assertEquals("entry_8", data.get(1).getEntry());
    assertEquals(DataType.NOTE, data.get(1).getType());
    assertEquals(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"), data.get(1).getAccount().getPublicId());
    assertNotNull(data.get(1).getCreatedAt());
    assertNotNull(data.get(1).getUpdatedAt());
    assertEquals(0, data.get(1).getVersion());
  }

  @Test
  @DisplayName("should return an empty list if user has no data")
  void shouldReturnEmptyListIfUserHasNoData() {
    List<Data> data =
        dataRepository
            .findAllByTypeAndAccountPublicId(DataType.PASSWORD, UUID.fromString("f01048b2-622a-49b6-963e-5e8edeec8026"));

    assertEquals(0, data.size());
  }

  @Test
  @DisplayName("should return an empty optional if data with given public id does not exist")
  void shouldReturnEmptyOptionalIfDataWithGivenPublicIdDoesNotExist() {
    Optional<Data> data = dataRepository.findByPublicId(UUID.randomUUID());
    assertTrue(data.isEmpty());
  }

  @Test
  @DisplayName("should return site by public id")
  void shouldReturnPasswordByPublicId() {
    Data data =
        dataRepository
            .findByPublicId(UUID.fromString("c3a175d3-cb93-4418-a480-eaee21505a49"))
            .orElseThrow(() -> new NotFoundException("Not Found"));

    assertNotNull(data.getId());
    assertEquals(UUID.fromString("c3a175d3-cb93-4418-a480-eaee21505a49"), data.getPublicId());
    assertEquals("entry_12", data.getEntry());
    assertEquals(DataType.SITE, data.getType());
    assertEquals(UUID.fromString("c3a175d3-cb93-4418-a480-eaee21505a49"), data.getPublicId());
    assertNotNull(data.getCreatedAt());
    assertNotNull(data.getUpdatedAt());
    assertEquals(0, data.getVersion());
  }

  @Test
  @DisplayName("should update entry with given public id")
  void shouldUpdateEntryWithGivenPublicId() {
    int updated =
        dataRepository.updateData("updated_entry", UUID.fromString("f4ed5604-667e-4395-9461-59b9a356b431"));

    em.flush();

    Data data =
        dataRepository
            .findByPublicId(UUID.fromString("f4ed5604-667e-4395-9461-59b9a356b431"))
                .orElseThrow(() -> new NotFoundException("Not Found"));

    assertEquals(1, updated);
    assertEquals("updated_entry", data.getEntry());
  }

  @Test
  @DisplayName("should not updated data")
  void shouldNotUpdateData() {
    int updated =
        dataRepository.updateData("new_data", UUID.randomUUID());

    assertEquals(0, updated);
  }

  @Test
  @DisplayName("should delete data by public id")
  void shouldDeleteDataByPublicId() {
    int deleted =
        dataRepository.deleteData(UUID.fromString("d088d359-c608-4d76-bf2d-60e0ec0f8fb7"));

    assertEquals(1, deleted);
  }

  @Test
  @DisplayName("should not delete data by public id")
  void shouldNotDeleteDataByPublicId() {
    int deleted = dataRepository.deleteData(UUID.randomUUID());
    assertEquals(0, deleted);
  }

  @Test
  @DisplayName("should delete all data that belong to dorciad")
  void shouldDeleteAllDataThatBelongToDorciad() {
    long deleted = dataRepository.deleteByAccountPublicId(UUID.fromString("e455b70f-50c5-4a96-9386-58f6ab9ba24b"));
    assertEquals(7, deleted);
  }

  @Test
  @DisplayName("should find all data that belong to dominik.krenski")
  void shouldFindAllDataDataThatBelongToDominikKrenski() {
    List<Data> data =
        dataRepository
            .findAllByAccountPublicId(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"));

    assertEquals(8, data.size());
  }

  @Test
  @DisplayName("should count all data belonging to dominik.krenski@gmail.com")
  void shouldCountAllDataBelongingToDominikKrenski() {
    long result = dataRepository.countByAccountPublicId(UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"));

    assertEquals(8, result);
  }

  @Test
  @DisplayName("should count all data belonging to dorciad")
  void shouldCountAllDataBelongingToDorciad() {
    long result = dataRepository.countByAccountPublicId(UUID.fromString("e455b70f-50c5-4a96-9386-58f6ab9ba24b"));

    assertEquals(7, result);
  }

  @Test
  @DisplayName("shoulr count all data belonging to dominik")
  void shouldCountAllDataBelongingToDominik() {
    long result = dataRepository.countByAccountPublicId(UUID.fromString("f01048b2-622a-49b6-963e-5e8edeec8026"));

    assertEquals(0, result);
  }
}