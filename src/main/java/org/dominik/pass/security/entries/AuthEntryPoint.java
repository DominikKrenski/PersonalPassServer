package org.dominik.pass.security.entries;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.dominik.pass.errors.api.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@RequiredArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper mapper;

  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) throws IOException, ServletException {
    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.UNAUTHORIZED)
        .message("User cannot be authenticated")
        .timestamp(Instant.now())
        .build();

    res.getWriter().write(mapper.writeValueAsString(apiError));
  }
}
