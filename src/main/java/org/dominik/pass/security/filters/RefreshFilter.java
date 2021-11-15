package org.dominik.pass.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.data.dto.RefreshTokenDTO;
import org.dominik.pass.errors.api.ApiError;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public final class RefreshFilter extends OncePerRequestFilter {
  private static final String URL = "/auth/refresh";
  private static final String METHOD = "GET";
  private static final String HEADER = "Authorization";
  private static final String SCHEME = "Bearer ";

  @NonNull private final ObjectMapper mapper;
  @NonNull private final JwtUtils jwtUtils;
  @NonNull private final RefreshTokenService refreshTokenService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    // check if URL is `/auth/refresh`, if not pass request to the next filter in the chain
    String url = request.getRequestURI().substring(request.getContextPath().length());

    if (!url.equals(URL)) {
      log.debug("Next filter invoked, because URL is: " + url);
      filterChain.doFilter(request, response);
      return;
    }

    // check if request method is GET
    if (!request.getMethod().equalsIgnoreCase(METHOD)) {
      log.error("Request method is invalid: " + request.getMethod());
      SecurityContextHolder.clearContext();
      prepareErrorResponse(response, "Request method is not supported");
      return;
    }

    // get Authorization header, if header is null return FORBIDDEN response
    String header = request.getHeader(HEADER);

    if (header == null) {
      log.error("Authorization header is missing");
      SecurityContextHolder.clearContext();
      prepareErrorResponse(response, "Required header is missing");
      return;
    }

    // get token from header, if header is null return FORBIDDEN response
    String token = getToken(header);

    if (token == null || token.length() == 0) {
      log.error("Schema missing or invalid");
      SecurityContextHolder.clearContext();
      prepareErrorResponse(response, "Scheme missing or invalid");
      return;
    }

    try {
      String subject =  jwtUtils.readSubject(token, JwtUtils.TokenType.REFRESH_TOKEN);
      RefreshTokenDTO refreshTokenDTO = refreshTokenService.findByToken(token);

      if (refreshTokenDTO.isUsed()) {
        // if refresh token has been used for the second time, delete all user tokens and return security message
        refreshTokenService.deleteAllAccountTokens(subject);
        SecurityContextHolder.clearContext();
        prepareErrorResponse(response, "Security Exception. Server detected that the same token has been used again");
      } else {
        // generate new pair of tokens
        String accessToken = jwtUtils.createToken(subject, JwtUtils.TokenType.ACCESS_TOKEN);
        String refreshToken = jwtUtils.createToken(subject, JwtUtils.TokenType.REFRESH_TOKEN);

        // save new refresh token and mark old refresh token as used
        refreshTokenService.saveNewRefreshToken(token, refreshToken, subject);

        // send new pair of tokens to the user
        AuthDTO authDTO = AuthDTO.builder().accessToken(accessToken).refreshToken(refreshToken).build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(mapper.writeValueAsString(authDTO));
      }
    } catch (JwtException ex) {
      prepareErrorResponse(response, "Token is not valid");
    } catch (NotFoundException ex) {
      prepareErrorResponse(response, ex.getMessage());
    } catch (IllegalArgumentException ex) {
      prepareErrorResponse(response, "Subject has invalid format");
    }
  }

  private String getToken(String content) {
    // check if content starts with `Bearer `, if not return null
    if (!content.startsWith(SCHEME))
      return null;

    // get token
    return content.substring(7);
  }

  private void prepareErrorResponse(HttpServletResponse response, String message) throws IOException {
    ApiError apiError = ApiError.builder()
        .status(HttpStatus.FORBIDDEN)
        .timestamp(Instant.now())
        .message(message)
        .build();

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.getWriter().write(mapper.writeValueAsString(apiError));
  }
}
