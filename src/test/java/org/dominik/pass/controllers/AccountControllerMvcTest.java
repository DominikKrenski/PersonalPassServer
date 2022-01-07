package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.configuration.ApiControllerMvcTestConfig;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.UpdatePasswordDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.errors.exceptions.ConflictException;
import org.dominik.pass.errors.exceptions.DataNumberException;
import org.dominik.pass.errors.exceptions.InternalException;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.DataService;
import org.dominik.pass.services.definitions.EmailService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
  value = AccountController.class,
  excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class},
  properties = {
    "spring.main.banner-mode=off"
  }
)
@WithMockUser
@Import(ApiControllerMvcTestConfig.class)
@ActiveProfiles("test")
class AccountControllerMvcTest {
  private static final String ACCOUNT_URL = "/accounts/";
  private static final String EMAIL_URL = "/accounts/email";
  private static final String HINT_URL = "/accounts/hint";
  private static final String TEST_URL = "/accounts/test-email";
  private static final Long ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_USER;
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();
  private static final short VERSION = 0;


  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static Properties props;
  private static Account account;

  @Autowired
  MockMvc mvc;
  @Autowired
  ObjectMapper mapper;
  @Autowired
  SecurityUtils securityUtils;
  @Autowired
  EmailService emailService;
  @Autowired
  AccountService accountService;
  @Autowired
  DataService dataService;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    props = readPropertiesFile("ValidationMessages.properties");
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
  }

  @Test
  @DisplayName("should return info about account")
  void shouldReturnInfoAboutAccount() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(accountService.findByEmail(anyString())).thenReturn(AccountDTO.fromAccount(account));
    when(accountService.existsByEmail(anyString())).thenReturn(false);

    mvc
      .perform(
        get(ACCOUNT_URL)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value(EMAIL))
      .andExpect(jsonPath("$.reminder").value(REMINDER))
      .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(CREATED_AT)))
      .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(UPDATED_AT)));
  }

  @Test
  @DisplayName("should return NotFound if account does not exist")
  void shouldReturnNotFoundIfAccountDoesNotExist() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(accountService.findByEmail(anyString())).thenThrow(new NotFoundException("Account does not exist"));
    when(accountService.existsByEmail(anyString())).thenReturn(false);

    mvc
      .perform(
        get(ACCOUNT_URL)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Account does not exist"));
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  @DisplayName("should throw Forbidden if user has role ADMIN")
  void shouldThrowForbiddenIfUserHasRoleAdmin() throws Exception {
    mvc
      .perform(
        get(ACCOUNT_URL)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("You are not allowed to access this resource"));
  }

  @Test
  @DisplayName("should return Conflict if email is already in use")
  void shouldReturnConflictIfEmailIsAlreadyInUse() throws Exception {
    String body = """
      {
        "email": "dominik.krenski@gmail.com"
      }
      """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(accountService.updateEmail(anyString(), anyString())).thenThrow(new ConflictException("Email is already in use"));

    mvc
      .perform(
        put(EMAIL_URL)
          .content(body)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Email is already in use"));
  }

  @Test
  @DisplayName("should return InternalException if email could not be updated")
  void shouldReturnInternalExceptionIfEmailNotUpdated() throws Exception {
    String body = """
      {
        "email": "dominik.krenski@gmail.com"
      }
      """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(accountService.updateEmail(anyString(), anyString())).thenThrow(new InternalException("Email could not be updated"));

    mvc
      .perform(
        put(EMAIL_URL)
          .content(body)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Email could not be updated"));
  }

  @Test
  @DisplayName("should return NotFound if updated account could not be found")
  void shouldReturnNotFoundIfUpdatedAccountNotFound() throws Exception {
    String body = """
      {
        "email": "dominik.krenski@gmail.com"
      }
      """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(accountService.updateEmail(anyString(), anyString())).thenThrow(new NotFoundException("Account does not exist"));

    mvc
      .perform(
        put(EMAIL_URL)
          .content(body)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Account does not exist"));
  }

  @Test
  @DisplayName("should return updated account info")
  void shouldReturnUpdatedAccountInfo() throws Exception {
    String body = """
      {
        "email": "dominik.krenski@gmail.com"
      }
      """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(accountService.updateEmail(anyString(), anyString())).thenReturn(AccountDTO.fromAccount(account));

    mvc
      .perform(
        put(EMAIL_URL)
          .content(body)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value(account.getEmail()))
      .andExpect(jsonPath("$.reminder").value(account.getReminder()))
      .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(account.getCreatedAt())))
      .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(account.getUpdatedAt())));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if email is not valid")
  void shouldReturnUnprocessableEntityIfEmailIsNotValid() throws Exception {
    List<String> emailMessages = new LinkedList<>(
      List.of(
        props.getProperty("email.blank.message"),
        props.getProperty("email.format.message"))
    );

    String data = """
      {
        "email": ""
      }
      """;

    mvc
      .perform(
        put(EMAIL_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();

        ReadContext ctx = JsonPath.parse(body);

        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<>() {
          })
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("email", map.get("email").getField());
        assertEquals("", map.get("email").getRejectedValue());
        assertTrue(map.get("email").getValidationMessages().containsAll(emailMessages));
      });
  }

  @Test
  @DisplayName("should send reminder email")
  void shouldSendReminderEmail() throws Exception {
    String data = """
      {
        "email": "dominik.krenski@gmail.com"
      }
      """;

    when(emailService.sendHint(anyString())).thenReturn("email-id");

    mvc
      .perform(
        post(HINT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.emailId").value("email-id"));
  }

  @Test
  @DisplayName("should return NotFound if account does not exist")
  void shouldReturnNotFoundIfAccountNotExists() throws Exception {
    String data = """
      {
        "email": "dominik.krenski@gmail.com"
      }
      """;

    when(emailService.sendHint(anyString())).thenThrow(new NotFoundException("Account does not exist"));

    mvc
      .perform(
        post(HINT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Account does not exist"));
  }

  @Test
  @DisplayName("should return InternalException if email could not be send")
  void shouldReturnInternalExceptionIfEmailCouldNotBeSend() throws Exception {
    String data = """
      {
        "email": "dominik.krenski@gmail.com"
      }
      """;

    when(emailService.sendHint(anyString())).thenThrow(new InternalException("Email problem"));

    mvc
      .perform(
        post(HINT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Email problem"));
  }

  @Test
  @DisplayName("should return InternalException if test email could not be send")
  void shouldReturnInternalExceptionIfTestEmailCouldNotBeSend() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(emailService.sendTestEmail(anyString())).thenThrow(new InternalException("There is a problem"));

    mvc
      .perform(
        get(TEST_URL)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("There is a problem"));
  }

  @Test
  @DisplayName("should send test email successfully")
  void shouldSendTestEmailSuccessfully() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(emailService.sendTestEmail(anyString())).thenReturn("message-id");

    mvc
      .perform(
        get(TEST_URL)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.emailId").value("message-id"));
  }

  @Test
  @DisplayName("should return NofFound if account to be deleted does not exist")
  void shouldReturnNotFoundIfAccountToBeDeletedDoesNotExist() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doThrow(new NotFoundException("Account does not exist")).when(accountService).deleteAccount(any(UUID.class));

    mvc
      .perform(
        delete(ACCOUNT_URL)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
      .andExpect(jsonPath("$.message").value("Account does not exist"))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should delete account")
  void shouldDeleteAccount() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doNothing().when(accountService).deleteAccount(any(UUID.class));

    mvc
      .perform(
        delete(ACCOUNT_URL)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return NotFound if salt for given account does not exist")
  void shouldReturnNotFoundIfSaltForGivenAccountDoesNotExist() throws Exception {
    when(securityUtils.getPrincipal()).thenThrow(new NotFoundException("Account does not exist"));

    mvc
      .perform(
        get(ACCOUNT_URL + "/salt")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
      .andExpect(jsonPath("$.message").value("Account does not exist"))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return salt for given account")
  void shouldReturnSaltForGivenAccount() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(accountService.findByPublicId(any(UUID.class))).thenReturn(AccountDTO.fromAccount(account));

    mvc
      .perform(
        get(ACCOUNT_URL + "/salt")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.salt").value(account.getSalt()));
  }

  @Test
  @DisplayName("should update password and all related data")
  void shouldUpdatePasswordAndAllRelatedData() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doNothing().when(dataService).updateAllData(any(UUID.class), any(UpdatePasswordDTO.class));

    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2ccf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "publicId": "9a998cd6-71b1-442b-b663-16aad7b499f3",
            "entry": "2849eca061b3b17b3ad0b095.09462cb2d42e13694fa5a3179f0a5c8e74dffaa4f3f7ffd7782643fcbe1d9f3056530da982b412aea37f94f34d71e3fdadc1a735b3b0adb906105e0f84ef73339c6bc829748474d41f491ebc7967f768cb65bc6f5b8a9cc92f43c3689d59f55afbe9a67c302f442454c4f694b36b8135cd4573f56996765ae1577fe02a8a14eb7f93eaf2b1d26efbde59a55cbdd78022566a2d44fd8d29c46dc023baa999087d1c995ddc4a1b156b74349f88b26101119f51df67da71ed6f55c37b129da046ff925aaa7df9f9bd712e62fad469e1f00aa37825395640eb6565bca85f11d2ad66b6fef5b4536acb4339005d53256060f31e627c544913b2b72e686777eea0e29863a9848df078b69fb23d8759bbed3bb8295ee03bd96c958e9cad8d1ea02614e3a625d1e83f78f453c06fdddb3c95a8225dcdcfab0934500f5a4dccd350870dfa41f8",
            "type": "ADDRESS",
            "createdAt": "04/01/2022T16:00:36.405Z",
            "updatedAt": "04/01/2022T16:00:36.405Z"
          },
          {
            "publicId": "2d98f03e-17bd-4ab8-bab6-9486b8ca4eac",
            "entry": "247dc6ece51a1607bb861602.262468cfc68e373fa32a6a1a8684828f7642118baac452f5ab3efc1081523adf1cd77b9008f7f23b0476e5a8801e468ba58fb516b027b293516ac11c38bab024d0cb666fa5b5e556e60f05744513ae",
            "type": "NOTE",
            "createdAt": "12/12/2021T13:48:22.345Z",
            "updatedAt": "12/12/2021T13:48:22.345Z"
          },
          {
            "publicId": "1274bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "type": "SITE",
            "createdAt": "01/10/2021T15:28:56.444Z",
            "updatedAt": "01/10/2021T15:28:56.444Z"
          }
        ]
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should update password with no data")
  void shouldUpdatePasswordWithNoData() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2ccf26f9ce73bacf6b8bec8c51e93457",
        "data": []
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return BadRequest if data numbers are different")
  void shouldReturnBadRequestIfDataNumbersAreDifferent() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doThrow(new DataNumberException("Invalid data number")).when(dataService).updateAllData(any(UUID.class), any(UpdatePasswordDTO.class));

    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2ccf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "publicId": "9a998cd6-71b1-442b-b663-16aad7b499f3",
            "entry": "2849eca061b3b17b3ad0b095.09462cb2d42e13694fa5a3179f0a5c8e74dffaa4f3f7ffd7782643fcbe1d9f3056530da982b412aea37f94f34d71e3fdadc1a735b3b0adb906105e0f84ef73339c6bc829748474d41f491ebc7967f768cb65bc6f5b8a9cc92f43c3689d59f55afbe9a67c302f442454c4f694b36b8135cd4573f56996765ae1577fe02a8a14eb7f93eaf2b1d26efbde59a55cbdd78022566a2d44fd8d29c46dc023baa999087d1c995ddc4a1b156b74349f88b26101119f51df67da71ed6f55c37b129da046ff925aaa7df9f9bd712e62fad469e1f00aa37825395640eb6565bca85f11d2ad66b6fef5b4536acb4339005d53256060f31e627c544913b2b72e686777eea0e29863a9848df078b69fb23d8759bbed3bb8295ee03bd96c958e9cad8d1ea02614e3a625d1e83f78f453c06fdddb3c95a8225dcdcfab0934500f5a4dccd350870dfa41f8",
            "type": "ADDRESS",
            "createdAt": "04/01/2022T16:00:36.405Z",
            "updatedAt": "04/01/2022T16:00:36.405Z"
          },
          {
            "publicId": "2d98f03e-17bd-4ab8-bab6-9486b8ca4eac",
            "entry": "247dc6ece51a1607bb861602.262468cfc68e373fa32a6a1a8684828f7642118baac452f5ab3efc1081523adf1cd77b9008f7f23b0476e5a8801e468ba58fb516b027b293516ac11c38bab024d0cb666fa5b5e556e60f05744513ae",
            "type": "NOTE",
            "createdAt": "12/12/2021T13:48:22.345Z",
            "updatedAt": "12/12/2021T13:48:22.345Z"
          },
          {
            "publicId": "1274bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "type": "SITE",
            "createdAt": "01/10/2021T15:28:56.444Z",
            "updatedAt": "01/10/2021T15:28:56.444Z"
          }
        ]
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Invalid data number"));
  }

  @Test
  @DisplayName("should return NotFound if one of the data could not be updated")
  void shouldReturnNotFoundIfOneOfTheDataCouldNotBeUpdated() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));

    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2ccf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "publicId": "9a998cd6-71b1-442b-b663-16aad7b499f3",
            "entry": "2849eca061b3b17b3ad0b095.09462cb2d42e13694fa5a3179f0a5c8e74dffaa4f3f7ffd7782643fcbe1d9f3056530da982b412aea37f94f34d71e3fdadc1a735b3b0adb906105e0f84ef73339c6bc829748474d41f491ebc7967f768cb65bc6f5b8a9cc92f43c3689d59f55afbe9a67c302f442454c4f694b36b8135cd4573f56996765ae1577fe02a8a14eb7f93eaf2b1d26efbde59a55cbdd78022566a2d44fd8d29c46dc023baa999087d1c995ddc4a1b156b74349f88b26101119f51df67da71ed6f55c37b129da046ff925aaa7df9f9bd712e62fad469e1f00aa37825395640eb6565bca85f11d2ad66b6fef5b4536acb4339005d53256060f31e627c544913b2b72e686777eea0e29863a9848df078b69fb23d8759bbed3bb8295ee03bd96c958e9cad8d1ea02614e3a625d1e83f78f453c06fdddb3c95a8225dcdcfab0934500f5a4dccd350870dfa41f8",
            "type": "ADDRESS",
            "createdAt": "04/01/2022T16:00:36.405Z",
            "updatedAt": "04/01/2022T16:00:36.405Z"
          },
          {
            "publicId": "2d98f03e-17bd-4ab8-bab6-9486b8ca4eac",
            "entry": "247dc6ece51a1607bb861602.262468cfc68e373fa32a6a1a8684828f7642118baac452f5ab3efc1081523adf1cd77b9008f7f23b0476e5a8801e468ba58fb516b027b293516ac11c38bab024d0cb666fa5b5e556e60f05744513ae",
            "type": "NOTE",
            "createdAt": "12/12/2021T13:48:22.345Z",
            "updatedAt": "12/12/2021T13:48:22.345Z"
          },
          {
            "publicId": "1274bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "type": "SITE",
            "createdAt": "01/10/2021T15:28:56.444Z",
            "updatedAt": "01/10/2021T15:28:56.444Z"
          }
        ]
      }
      """;

    doThrow(new NotFoundException("msg")).when(dataService).updateAllData(any(UUID.class), any(UpdatePasswordDTO.class));

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("msg"));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if password is 63 characters long")
  void shouldReturnUnprocessableEntityIfPasswordIs63CharactersLong() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f5",
        "salt": "2ccf26f9ce73bacf6b8bec8c51e93457"
      }
      """;

    List<String> messages = new LinkedList<>(
      List.of(
        props.getProperty("password.length.message").replace("{max}", "64"),
        props.getProperty("password.hex.message")
      )
    );

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();
        ReadContext ctx = JsonPath.parse(body);
        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<List<TestValidationError>>(){})
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("password", map.get("password").getField());
        assertEquals("6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f5", map.get("password").getRejectedValue());
        assertTrue(map.get("password").getValidationMessages().containsAll(messages));
      });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if password has invalid format")
  void shouldReturnUnprocessableEntityIfPasswordHasInvalidFormat() throws Exception {
    String data = """
      {
        "password": "26gc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f5",
        "salt": "2ccf26f9ce73bacf6b8bec8c51e93457"
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();
        ReadContext ctx = JsonPath.parse(body);
        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<List<TestValidationError>>(){})
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("password", map.get("password").getField());
        assertEquals("26gc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f5", map.get("password").getRejectedValue());
        assertTrue(map.get("password").getValidationMessages().contains(props.getProperty("password.hex.message")));
      });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if password is not present")
  void shouldReturnUnprocessableEntityIfPasswordIsNotPresent() throws Exception {
    String data = """
      {
        "salt": "2ccf26f9ce73bacf6b8bec8c51e93457"
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();
        ReadContext ctx = JsonPath.parse(body);
        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<List<TestValidationError>>(){})
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("password", map.get("password").getField());
        assertNull(map.get("password").getRejectedValue());
        assertTrue(map.get("password").getValidationMessages().containsAll(
          List.of(
            props.getProperty("password.blank.message"),
            props.getProperty("password.hex.message")
          )
        ));
      });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if salt is 31 characters long")
  void shouldReturnUnprocessableEntityIfSaltIs30CharactersLong() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "cdf26f9ce73bacf6b8bec8c51e93457"
      }
      """;

    List<String> messages = new LinkedList<>(
      List.of(
        props.getProperty("salt.length.message").replace("{max}", "32"),
        props.getProperty("salt.hex.message")
      )
    );

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();
        ReadContext ctx = JsonPath.parse(body);
        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<List<TestValidationError>>(){})
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("salt", map.get("salt").getField());
        assertEquals("cdf26f9ce73bacf6b8bec8c51e93457", map.get("salt").getRejectedValue());
        assertTrue(map.get("salt").getValidationMessages().containsAll(messages));
      });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if salt has not valid format")
  void shouldReturnUnprocessableEntityIfSaltHasNotValidFormat() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2crf26f9ce73bacf6b8bec8c51e93457"
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();
        ReadContext ctx = JsonPath.parse(body);
        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<List<TestValidationError>>(){})
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("salt", map.get("salt").getField());
        assertEquals("2crf26f9ce73bacf6b8bec8c51e93457", map.get("salt").getRejectedValue());
        assertTrue(map.get("salt").getValidationMessages().contains(props.getProperty("salt.hex.message")));
      });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if salt consists of 2 spaces")
  void shouldReturnUnprocessableEntityIfSaltConsistsOf2Spaces() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "  "
      }
      """;

    List<String> messages = new LinkedList<>(
      List.of(
        props.getProperty("salt.blank.message"),
        props.getProperty("salt.length.message").replace("{max}", "32"),
        props.getProperty("salt.hex.message")
      )
    );

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();
        ReadContext ctx = JsonPath.parse(body);
        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<List<TestValidationError>>(){})
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("salt", map.get("salt").getField());
        assertEquals("  ", map.get("salt").getRejectedValue());
        assertTrue(map.get("salt").getValidationMessages().containsAll(messages));
      });
  }

  @Test
  @DisplayName("should return BadRequest if data public id is not valid")
  void shouldReturnBadRequestIfDataPublicIdIsNotValid() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2caf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "publicId": "9a998cd6-71b1-442b-b663-16aad7b499f3q",
            "entry": "2849eca061b3b17b3ad0b095.09462cb2d42e13694fa5a3179f0a5c8e74dffaa4f3f7ffd7782643fcbe1d9f3056530da982b412aea37f94f34d71e3fdadc1a735b3b0adb906105e0f84ef73339c6bc829748474d41f491ebc7967f768cb65bc6f5b8a9cc92f43c3689d59f55afbe9a67c302f442454c4f694b36b8135cd4573f56996765ae1577fe02a8a14eb7f93eaf2b1d26efbde59a55cbdd78022566a2d44fd8d29c46dc023baa999087d1c995ddc4a1b156b74349f88b26101119f51df67da71ed6f55c37b129da046ff925aaa7df9f9bd712e62fad469e1f00aa37825395640eb6565bca85f11d2ad66b6fef5b4536acb4339005d53256060f31e627c544913b2b72e686777eea0e29863a9848df078b69fb23d8759bbed3bb8295ee03bd96c958e9cad8d1ea02614e3a625d1e83f78f453c06fdddb3c95a8225dcdcfab0934500f5a4dccd350870dfa41f8",
            "type": "ADDRESS",
            "createdAt": "10/02/2021T15:54:23.123Z",
            "updatedAt": "10/02/2021T15:54:23.123Z"
          },
          {
            "publicId": "134574bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "type": "SITE",
            "createdAt": "01/01/2022T09:59:34.856Z",
            "updatedAt": "01/01/2022T09:59:34.856Z"
          }
        ]
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Message is not formatted properly"));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if publicId is missing")
  void shouldReturnUnprocessableEntityIfPublicIdIsMissing() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2caf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "entry": "2849eca061b3b17b3ad0b095.09462cb2d42e13694fa5a3179f0a5c8e74dffaa4f3f7ffd7782643fcbe1d9f3056530da982b412aea37f94f34d71e3fdadc1a735b3b0adb906105e0f84ef73339c6bc829748474d41f491ebc7967f768cb65bc6f5b8a9cc92f43c3689d59f55afbe9a67c302f442454c4f694b36b8135cd4573f56996765ae1577fe02a8a14eb7f93eaf2b1d26efbde59a55cbdd78022566a2d44fd8d29c46dc023baa999087d1c995ddc4a1b156b74349f88b26101119f51df67da71ed6f55c37b129da046ff925aaa7df9f9bd712e62fad469e1f00aa37825395640eb6565bca85f11d2ad66b6fef5b4536acb4339005d53256060f31e627c544913b2b72e686777eea0e29863a9848df078b69fb23d8759bbed3bb8295ee03bd96c958e9cad8d1ea02614e3a625d1e83f78f453c06fdddb3c95a8225dcdcfab0934500f5a4dccd350870dfa41f8",
            "type": "ADDRESS",
            "createdAt": "10/02/2021T15:54:23.123Z",
            "updatedAt": "10/02/2021T15:54:23.123Z"
          },
          {
            "publicId": "1274bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "type": "SITE",
            "createdAt": "01/01/2022T09:59:34.856Z",
            "updatedAt": "01/01/2022T09:59:34.856Z"
          }
        ]
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();
        ReadContext ctx = JsonPath.parse(body);
        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<List<TestValidationError>>(){})
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("data[0].publicId", map.get("data[0].publicId").getField());
        assertNull(map.get("data[0].publicId").getRejectedValue());
        assertTrue(map.get("data[0].publicId").getValidationMessages().contains(props.getProperty("public_id.null.message")));
      });
  }

  @Test
  @DisplayName("should return BadRequest if type is not valid")
  void shouldReturnBadRequestIfTypeIsNotValid() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2caf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "publicId": "9a998cd6-71b1-442b-b663-16aad7b499f3",
            "entry": "2849eca061b3b17b3ad0b095.09462cb2d42e13694fa5a3179f0a5c8e74dffaa4f3f7ffd7782643fcbe1d9f3056530da982b412aea37f94f34d71e3fdadc1a735b3b0adb906105e0f84ef73339c6bc829748474d41f491ebc7967f768cb65bc6f5b8a9cc92f43c3689d59f55afbe9a67c302f442454c4f694b36b8135cd4573f56996765ae1577fe02a8a14eb7f93eaf2b1d26efbde59a55cbdd78022566a2d44fd8d29c46dc023baa999087d1c995ddc4a1b156b74349f88b26101119f51df67da71ed6f55c37b129da046ff925aaa7df9f9bd712e62fad469e1f00aa37825395640eb6565bca85f11d2ad66b6fef5b4536acb4339005d53256060f31e627c544913b2b72e686777eea0e29863a9848df078b69fb23d8759bbed3bb8295ee03bd96c958e9cad8d1ea02614e3a625d1e83f78f453c06fdddb3c95a8225dcdcfab0934500f5a4dccd350870dfa41f8",
            "type": "UNKNOWN",
            "createdAt": "10/02/2021T15:54:23.123Z",
            "updatedAt": "10/02/2021T15:54:23.123Z"
          },
          {
            "publicId": "1274bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "type": "SITE",
            "createdAt": "01/01/2022T09:59:34.856Z",
            "updatedAt": "01/01/2022T09:59:34.856Z"
          }
        ]
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Message is not formatted properly"));
  }

  @Test
  @DisplayName("should return BadRequest if createdAt is not valid")
  void shouldReturnBadRequestIfCreatedAtIsNotValid() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2caf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "publicId": "1274bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "type": "ADDRESS",
            "createdAt": "01-01-2022T09:59:34.856Z",
            "updatedAt": "01/01/2022T09:59:34.856Z"
          }
        ]
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Message is not formatted properly"));
  }

  @Test
  @DisplayName("should return UnprocessableEntity is type is missing")
  void shouldReturnUnprocessableEntityIfTypeIsMissing() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2caf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "publicId": "1274bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "createdAt": "01/01/2022T09:59:34.856Z",
            "updatedAt": "01/01/2022T09:59:34.856Z"
          }
        ]
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();
        ReadContext ctx = JsonPath.parse(body);
        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<List<TestValidationError>>(){})
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("data[0].type", map.get("data[0].type").getField());
        assertNull(map.get("data[0].type").getRejectedValue());
        assertTrue(map.get("data[0].type").getValidationMessages().contains(props.getProperty("data.null.message")));
      });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if updatedAt is missing")
  void shouldReturnUnprocessableEntityIfUpdatedAtIsMissing() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2caf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "publicId": "1274bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "type": "ADDRESS"
            "createdAt": "01/01/2022T09:59:34.856Z",
          }
        ]
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
      .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
      .andExpect(jsonPath("$.message").value("Message is not formatted properly"));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if data consists of 2 spaces")
  void shouldReturnUnprocessableEntityIfDataConsistsOf2Spaces() throws Exception {
    String data = """
      {
        "password": "6fc2628963977933f9e42914c88c79c136a9a6fb7e896d9131b5f88c3dfe1f56",
        "salt": "2ccf26f9ce73bacf6b8bec8c51e93457",
        "data": [
          {
            "publicId": "9a998cd6-71b1-442b-b663-16aad7b499f3",
            "entry": "  ",
            "type": "ADDRESS",
            "createdAt": "04/01/2022T16:00:36.405Z",
            "updatedAt": "04/01/2022T16:00:36.405Z"
          },
          {
            "publicId": "2d98f03e-17bd-4ab8-bab6-9486b8ca4eac",
            "entry": "247dc6ece51a1607bb861602.262468cfc68e373fa32a6a1a8684828f7642118baac452f5ab3efc1081523adf1cd77b9008f7f23b0476e5a8801e468ba58fb516b027b293516ac11c38bab024d0cb666fa5b5e556e60f05744513ae",
            "type": "NOTE",
            "createdAt": "12/12/2021T13:48:22.345Z",
            "updatedAt": "12/12/2021T13:48:22.345Z"
          },
          {
            "publicId": "1274bba6-cba9-4ce2-9461-e1b3b7e00cef",
            "entry": "185d054504ae6f3c9d5673e7.fedd5f2e980f2cf60ccdaf137d7f6908665d73edee3fd0c56c13bfd9df4da35850f2a329304d6ce4bdc8c4fd43609cc04313816fb1c0a79345b6f1ad9ea6f1fd41c3b3dd45631d",
            "type": "SITE",
            "createdAt": "01/10/2021T15:28:56.444Z",
            "updatedAt": "01/10/2021T15:28:56.444Z"
          }
        ]
      }
      """;

    mvc
      .perform(
        put(ACCOUNT_URL)
          .content(data)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnprocessableEntity())
      .andDo(res -> {
        String body = res.getResponse().getContentAsString();
        ReadContext ctx = JsonPath.parse(body);
        String errors = getSubErrorsString(body);

        Map<String, TestValidationError> map = convertErrorListToMap(
          mapper.readValue(errors, new TypeReference<List<TestValidationError>>(){})
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
        assertEquals("Validation Error", ctx.read("$.message"));
        assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
        assertEquals("data[0].entry", map.get("data[0].entry").getField());
        assertEquals("  ", map.get("data[0].entry").getRejectedValue());
        assertTrue(map.get("data[0].entry").getValidationMessages().containsAll(
          List.of(
            props.getProperty("data.blank.message"),
            props.getProperty("data.pattern.message")
          )
        ));
      });
  }


}
