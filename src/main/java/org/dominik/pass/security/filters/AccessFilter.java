package org.dominik.pass.security.filters;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AccountService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AccessFilter extends OncePerRequestFilter {
  private static final String HEADER = "Authorization";

  @NonNull private final AccountService accountService;
  @NonNull private final JwtUtils jwtUtils;
  @NonNull private final SecurityUtils securityUtils;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    // check if Authorization header is present, if not pass request to the next filter
    // in the chain, because this request should not be processed maybe
    String header = request.getHeader(HEADER);

    if (header == null) {
      filterChain.doFilter(request, response);
      return;
    }

    // get token from header
    String token = securityUtils.getToken(header);

    // if token is null or token is an empty string return FORBIDDEN response
    if (token == null || token.length() == 0) {
      log.error("Scheme missing or invalid");
      securityUtils.prepareForbiddenResponse(response, "Scheme missing or invalid");
      return;
    }

    try {
      // get public id from token
      String subject = jwtUtils.readSubject(token, JwtUtils.TokenType.ACCESS_TOKEN);

      // find account by public id
      AccountDTO accountDTO = accountService.findByPublicId(UUID.fromString(subject));

      // get account from database and convert it into UserDetails object
      AccountDetails accountDetails = AccountDetails.fromDTO(accountDTO);

      // create Authentication object
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(accountDetails, null, accountDetails.getAuthorities());

      // create empty SecurityContext
      SecurityContext context = SecurityContextHolder.createEmptyContext();

      // add Authentication object into SecurityContext
      context.setAuthentication(authToken);

      // add SecurityContext into SecurityContextHolder
      SecurityContextHolder.setContext(context);

      // go to the next filter in chain
      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException ex) {
      log.error(Arrays.toString(ex.getStackTrace()));
      securityUtils.prepareForbiddenResponse(response, "Access token expired");
    } catch (NotFoundException ex) {
      log.error(Arrays.toString(ex.getStackTrace()));
      securityUtils.prepareForbiddenResponse(response, "Account does not exist");
    } catch (Exception ex) {
      log.error(Arrays.toString(ex.getStackTrace()));
      securityUtils.prepareForbiddenResponse(response, "Access token is invalid");
    }
  }
}
