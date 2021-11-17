package org.dominik.pass.security.utils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.security.AccountDetails;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.dominik.pass.utils.TestUtils.createObjectMapperInstance;
import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {
  private static final String SCHEME = "Bearer ";
  private static final Long ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_USER;
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();
  private static final short VERSION = 2;
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static SecurityUtils securityUtils;

  @BeforeAll
  static void setUp() {
    securityUtils = new SecurityUtils(createObjectMapperInstance());
  }

  @Test
  @DisplayName("should get principal from security context")
  void shouldGetPrincipalFromSecurityContext() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    // create Account instance
    Account account = createAccountInstance(
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

    var accountDetails = AccountDetails.fromDTO(AccountDTO.fromAccount(account));
    var token = new UsernamePasswordAuthenticationToken(accountDetails, null, accountDetails.getAuthorities());

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(token);
    SecurityContextHolder.setContext(context);

    AccountDetails details = securityUtils.getPrincipal();

    assertEquals(account.getEmail(), details.getUsername());
    assertEquals(account.getPublicId().toString(), details.getPublicId().toString());
    assertEquals(account.getPassword(), details.getPassword());
    assertTrue(details.isAccountNonExpired());
    assertTrue(details.isAccountNonLocked());
    assertTrue(details.isCredentialsNonExpired());
    assertTrue(details.isEnabled());
    assertTrue(details.getAuthorities().contains(new SimpleGrantedAuthority(ROLE.toString())));
  }

  @Test
  @DisplayName("should return null if Auth header does not start with `Bearer `")
  void  shouldReturnNullIfAuthHeaderDoesNotStartWithBearer() {
    String content = "refresh_token";

    assertNull(securityUtils.getToken(content));
  }

  @Test
  @DisplayName("should return null if Auth header starts with `Bearer`")
  void shouldReturnNullIfAuthHeaderStartsWithBearerWithoutSpace() {
    String content = "Bearerrefresh_token";

    assertNull(securityUtils.getToken(content));
  }

  @Test
  @DisplayName("should return an empty string if only `Bearer ` is present")
  void shouldReturnEmptyStringIfOnlyBearerIsPresent() {
    assertEquals("", securityUtils.getToken(SCHEME));
  }

  @Test
  @DisplayName("should return token if Auth header is correct")
  void shouldReturnTokenIfAuthHeaderIsCorrect() {
    String content = SCHEME + "access_token";

    assertEquals("access_token", securityUtils.getToken(content));
  }

  @Test
  @DisplayName("should prepare valid forbidden response")
  void shouldPrepareValidForbiddenResponse() throws IOException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    securityUtils.prepareForbiddenResponse(response, "Request is forbidden");

    String body = response.getContentAsString();

    ReadContext ctx = JsonPath.parse(body);

    assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), ctx.read("$.status"));
    assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
    assertEquals("Request is forbidden", ctx.read("$.message"));
  }
}
