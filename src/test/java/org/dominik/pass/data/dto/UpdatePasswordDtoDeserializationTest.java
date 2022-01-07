package org.dominik.pass.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.dominik.pass.utils.TestUtils.createObjectMapperInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdatePasswordDtoDeserializationTest {
  private static ObjectMapper mapper;

  @BeforeAll
  static void setUp() {
    mapper = createObjectMapperInstance();
  }

  @Test
  @DisplayName("should deserialize object with data list")
  void shouldDeserializeObjectWithDataList() throws JsonProcessingException {
    String json = """
      {
        "password": "password",
        "salt": "salt",
        "data": [
          {
            "publicId": "9a998cd6-71b1-442b-b663-16aad7b499f3",
            "entry": "2849eca061b3b17b3ad0b095.09462cb2d42e13694fa5a3179f0a5c8e74dffaa4f3f7ffd7782643fcbe1d9f3056530da982b412aea37f94f34d71e3fdadc1a735b3b0adb906105e0f84ef73339c6bc829748474d41f491ebc7967f768cb65bc6f5b8a9cc92f43c3689d59f55afbe9a67c302f442454c4f694b36b8135cd4573f56996765ae1577fe02a8a14eb7f93eaf2b1d26efbde59a55cbdd78022566a2d44fd8d29c46dc023baa999087d1c995ddc4a1b156b74349f88b26101119f51df67da71ed6f55c37b129da046ff925aaa7df9f9bd712e62fad469e1f00aa37825395640eb6565bca85f11d2ad66b6fef5b4536acb4339005d53256060f31e627c544913b2b72e686777eea0e29863a9848df078b69fb23d8759bbed3bb8295ee03bd96c958e9cad8d1ea02614e3a625d1e83f78f453c06fdddb3c95a8225dcdcfab0934500f5a4dccd350870dfa41f8",
            "type": "ADDRESS",
            "createdAt": "04/01/2022T16:00:36.405Z",
            "updatedAt": "04/01/2022T16:00:36.405Z"
          },
          {
            "publicId": "d0471244-2795-4e74-8ab8-8850f38e0935",
            "entry": "fa08fb639096dbe4c7ee66f9.0ea202ed3ed7ba34a8f33b0609d15e1c32fd561761b8edb19d4f72410f180fb4cbc1d5988bf6a8a3a50195e4e95aeffb0665009b4cdce68479674447cb0aac18ecf1f31f9234b7fc713f22f9aa530c536150c9f97188f55a69b45c5fad1de39d0ed6bede501b3a8e9a496238a4380cd4ecf89eb0add513bac6f067a49b1919973d8fbc12a3c3e7200840d53a86428bbe68ec7af43933fdbb921613bb71a01f",
            "type": "PASSWORD",
            "createdAt": "01/01/2022T08:13:23.999Z",
            "updatedAt": "01/01/2022T08:13:23.999Z"
          }
        ]
      }
      """;

    var item = mapper.readValue(json, UpdatePasswordDTO.class);

    assertEquals("password", item.getPassword());
    assertEquals("salt", item.getSalt());
    assertEquals(2, item.getData().size());
  }

  @Test
  @DisplayName("should deserialize object with empty data list")
  void shouldDeserializeObjectWithEmptyDataList() throws JsonProcessingException {
    String json = """
      {
        "password": "password",
        "salt": "salt"
      }
      """;

    var item = mapper.readValue(json, UpdatePasswordDTO.class);

    assertNull(item.getData());
  }

  @Test
  @DisplayName("should deserialize object with reminder")
  void shouldDeserializeObjectWithReminder() throws JsonProcessingException {
    String json = """
      {
        "password": "pass",
        "salt": "slt",
        "reminder": "rmdr"
      }
      """;

    var item = mapper.readValue(json, UpdatePasswordDTO.class);

    assertEquals("pass", item.getPassword());
    assertEquals("slt", item.getSalt());
    assertEquals("rmdr", item.getReminder());
  }

  @Test
  @DisplayName("should deserialize object without reminder")
  void shouldDeserializeObjectWithoutReminder() throws JsonProcessingException {
    String json = """
      {
        "password": "pass",
        "salt": "slt"
      }
      """;

    var item = mapper.readValue(json, UpdatePasswordDTO.class);

    assertNull(item.getReminder());
  }
}
