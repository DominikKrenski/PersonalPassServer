package org.dominik.pass.security.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dominik.pass.errors.api.ApiError;
import org.dominik.pass.security.AccountDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Component
public class SecurityUtils {
  private static final String SCHEME = "Bearer ";

  private final ObjectMapper mapper;

  @Autowired
  public SecurityUtils(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public String getToken(String content) {
    // check if content starts with SCHEME, if not return null
    if (!content.startsWith(SCHEME))
      return null;

    // return token
    return content.substring(SCHEME.length());
  }

  public void prepareForbiddenResponse(HttpServletResponse response, String message) throws IOException {
    ApiError apiError = ApiError
      .builder()
      .status(HttpStatus.FORBIDDEN)
      .timestamp(Instant.now())
      .message(message)
      .build();

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.getWriter().write(mapper.writeValueAsString(apiError));
  }

  public AccountDetails getPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (AccountDetails) authentication.getPrincipal();
  }
}
