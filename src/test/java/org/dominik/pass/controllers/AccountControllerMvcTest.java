package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.configuration.ApiControllerMvcTestConfig;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.errors.exceptions.InternalException;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.RefreshTokenService;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private static final String REMINDER_URL = "/accounts/reminder";
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

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JwtUtils jwtUtils;
    @Autowired SecurityUtils securityUtils;
    @Autowired AccountService accountService;
    @Autowired RefreshTokenService tokenService;

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
    @DisplayName("should return InternalException if email was not updated")
    void shouldReturnInternalExceptionIfEmailWasNotUpdated() throws Exception {
        String body = """
            {
              "email": "dominik.krenski@gmail.com"
            }
            """;

        when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
        when(accountService.existsByEmail(anyString())).thenReturn(false);
        when(jwtUtils.createToken(anyString(), eq(JwtUtils.TokenType.ACCESS_TOKEN))).thenReturn("access_token");
        when(jwtUtils.createToken(anyString(), eq(JwtUtils.TokenType.REFRESH_TOKEN))).thenReturn("refresh_token");

        doThrow(new InternalException("Email could not be updated"))
            .when(tokenService)
            .saveRefreshTokenAfterEmailUpdate(anyString(), anyString(), anyString());

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
    @DisplayName("should return NotFound if account with given email does not exist")
    void shouldReturnNotFoundIfAccountWithGivenEmailDoesNotExist() throws Exception {
        String body = """
            {
              "email": "dominik.krenski@gmail.com"
            }
            """;

        when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
        when(accountService.existsByEmail(anyString())).thenReturn(false);
        when(jwtUtils.createToken(anyString(), eq(JwtUtils.TokenType.ACCESS_TOKEN))).thenReturn("access_token");
        when(jwtUtils.createToken(anyString(), eq(JwtUtils.TokenType.REFRESH_TOKEN))).thenReturn("refresh_token");

        doThrow(new NotFoundException("Account does not exist"))
            .when(tokenService)
            .saveRefreshTokenAfterEmailUpdate(anyString(), anyString(), anyString());

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
    @DisplayName("should save refresh token after email update")
    void shouldSaveRefreshTokenAfterEmailUpdate() throws Exception {
        String body = """
            {
              "email": "dominik.krenski@gmail.com"
            }
            """;

        when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
        when(accountService.existsByEmail(anyString())).thenReturn(false);
        when(jwtUtils.createToken(anyString(), eq(JwtUtils.TokenType.ACCESS_TOKEN))).thenReturn("access_token");
        when(jwtUtils.createToken(anyString(), eq(JwtUtils.TokenType.REFRESH_TOKEN))).thenReturn("refresh_token");
        doNothing()
            .when(tokenService)
            .saveRefreshTokenAfterEmailUpdate(anyString(), anyString(), anyString());

        mvc
            .perform(
                put(EMAIL_URL)
                    .content(body)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access_token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh_token"));
    }

    @Test
    @DisplayName("should return Conflict if email is already in use")
    void shouldReturnConflictIfEmailIsAlreadyInUse() throws Exception {
        String body = """
            {
              "email": "dominik.krenski@gmail.com"
            }
            """;

        when(accountService.existsByEmail(anyString())).thenReturn(true);

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
                    mapper.readValue(errors, new TypeReference<>(){})
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
    @DisplayName("should return InternalException if reminder could not be updated")
    void shouldReturnInternalExceptionIfReminderCouldNotBeUpdated() throws Exception {
        String data = """
            {
              "reminder": "new reminder"
            }
            """;

        when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
        when(accountService.updateReminder(anyString(), anyString())).thenReturn(0);

        mvc
            .perform(
                put(REMINDER_URL)
                    .content(data)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()))
            .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
            .andExpect(jsonPath("$.message").value("Reminder cannot be updated"));
    }

    @Test
    @DisplayName("should return NoContent if reminder has been updated")
    void shouldReturnNoContentIfReminderHasBeenUpdated() throws Exception {
        String data = """
            {
              "reminder": "new reminder"
            }
            """;

        when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
        when(accountService.updateReminder(anyString(), anyString())).thenReturn(1);

        mvc
            .perform(
                put(REMINDER_URL)
                    .content(data)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should return UnprocessableEntity if reminder is not valid")
    void shouldReturnUnprocessableEntityIfReminderIsNotValid() throws Exception {
        List<String> reminderMessages = new LinkedList<>(
            List.of(props.getProperty("reminder.blank.message"))
        );

        String data = """
            {
              "reminder": " "
            }
            """;

        mvc
            .perform(
                put(REMINDER_URL)
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
                    mapper.readValue(errors, new TypeReference<>(){})
                );

                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
                assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
                assertEquals("Validation Error", ctx.read("$.message"));
                assertEquals("reminder", map.get("reminder").getField());
                assertEquals(" ", map.get("reminder").getRejectedValue());
                assertTrue(map.get("reminder").getValidationMessages().containsAll(reminderMessages));
            });
    }
}