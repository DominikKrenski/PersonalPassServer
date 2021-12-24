package org.dominik.pass.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dominik.pass.data.enums.DataType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.dominik.pass.utils.TestUtils.createObjectMapperInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EncryptedDataDtoDeserializationTest {
  private static String ENTRY = "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791";
  private static ObjectMapper mapper;

  @BeforeAll
  static void setUp() {
    mapper = createObjectMapperInstance();
  }

  @Test
  @DisplayName("should create address type")
  void shouldCreateAddressType() throws JsonProcessingException {
    String data = """
        {
          "type": "ADDRESS",
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    EncryptedDataDTO dto = mapper.readValue(data, EncryptedDataDTO.class);

    assertEquals(DataType.ADDRESS, dto.getType());
    assertEquals(ENTRY, dto.getEntry());
  }

  @Test
  @DisplayName("should create password type")
  void shouldCreatePasswordType() throws JsonProcessingException {
    String data = """
        {
          "type": "PASSWORD",
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    EncryptedDataDTO dto = mapper.readValue(data, EncryptedDataDTO.class);

    assertEquals(DataType.PASSWORD, dto.getType());
    assertEquals(ENTRY, dto.getEntry());
  }

  @Test
  @DisplayName("should create site type")
  void shouldCreateSiteType() throws JsonProcessingException {
    String data = """
        {
          "type": "SITE",
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    EncryptedDataDTO dto = mapper.readValue(data, EncryptedDataDTO.class);

    assertEquals(DataType.SITE, dto.getType());
    assertEquals(ENTRY, dto.getEntry());
  }

  @Test
  @DisplayName("should create not type")
  void shouldCreateNotType() throws JsonProcessingException {
    String data = """
        {
          "type": "NOTE",
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    EncryptedDataDTO dto = mapper.readValue(data, EncryptedDataDTO.class);

    assertEquals(DataType.NOTE, dto.getType());
    assertEquals(ENTRY, dto.getEntry());
  }

  @Test
  @DisplayName("should set type to null if not present")
  void shouldThrow() throws JsonProcessingException {
    String data = """
        {
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    EncryptedDataDTO dto = mapper.readValue(data, EncryptedDataDTO.class);

    assertNull(dto.getType());
    assertEquals(ENTRY, dto.getEntry());
  }
}
