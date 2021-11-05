package org.dominik.pass.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dominik.pass.configuration.AuthControllerMvcTestConfig;
import org.dominik.pass.controllers.AuthController;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = AuthController.class,
    excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class},
    properties = {
        "spring.main.banner-mode=off"
    }
)
@Import(AuthControllerMvcTestConfig.class)
@ActiveProfiles("test")
public class LoginFilterMvcTest {
  private static final String EMAIL ="dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy reminder";
  private static final Long ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now().minusSeconds(5000);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(3200);
  private static final short VERSION = 1;
  private static final String URL = "/auth/signin";
  private static final String TIMESTAMP_PATTERN="\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  @Autowired MockMvc mvc;
  @Autowired UserDetailsService detailsService;
  @Autowired PasswordEncoder encoder;  // because AuthenticationManager uses declared PasswordEncoder to compare password, it is necessary to encode password when mocking UserDetailsService
  @Autowired ObjectMapper mapper;
  @Autowired RefreshTokenService tokenService;
  @Autowired JwtUtils jwtUtils;

  @Test
  @DisplayName("should authenticate user")
  void shouldAuthenticateUser() throws Exception {
    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        encoder.encode(PASSWORD),
        SALT,
        REMINDER,
        Role.ROLE_USER,
        true,
        true,
        true,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    AccountDTO accountDTO = AccountDTO.fromAccount(account);
    Credentials creds = new Credentials(EMAIL, PASSWORD);

    when(detailsService.loadUserByUsername(anyString())).thenReturn(AccountDetails.fromDTO(accountDTO));
    when(jwtUtils.createToken(anyString(), eq(JwtUtils.TokenType.ACCESS_TOKEN))).thenReturn("access_token");
    when(jwtUtils.createToken(anyString(), eq(JwtUtils.TokenType.REFRESH_TOKEN))).thenReturn("refresh_token");
    doNothing().when(tokenService).login(anyString(), anyString());

    mvc
        .perform(
            post(URL)
                .content(mapper.writeValueAsString(creds))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access_token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh_token"));
  }

  @Test
  @DisplayName("should return authentication exception if email is not valid")
  void shouldReturnAuthenticationExceptionIfEmailIsNotValid() throws Exception {
    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        encoder.encode(PASSWORD),
        SALT,
        REMINDER,
        Role.ROLE_USER,
        true,
        true,
        true,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    Credentials creds = new Credentials(EMAIL, PASSWORD);

    when(detailsService.loadUserByUsername(anyString())).thenThrow(new UsernameNotFoundException("User not found"));

    mvc
        .perform(
            post(URL)
                .content(mapper.writeValueAsString(creds))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("should return authentication exception if password is not valid")
  void shouldReturnAuthenticationExceptionIfPasswordIsNotValid() throws Exception {
    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        encoder.encode("Dominik1984!"),
        SALT,
        REMINDER,
        Role.ROLE_USER,
        true,
        true,
        true,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    AccountDTO accountDTO = AccountDTO.fromAccount(account);
    Credentials creds = new Credentials(EMAIL, PASSWORD);

    when(detailsService.loadUserByUsername(anyString())).thenReturn(AccountDetails.fromDTO(accountDTO));

    mvc
        .perform(
            post(URL)
                .content(mapper.writeValueAsString(creds))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("should return authentication exception if method is GET")
  void shouldReturnAuthenticationExceptionIfMethodIsGet() throws Exception {
    mvc
        .perform(
            get(URL)
        )
        .andExpect(status().isUnauthorized());
  }

  @Getter
  @AllArgsConstructor
  private final static class Credentials {
    private String email;
    private String password;
  }
}
