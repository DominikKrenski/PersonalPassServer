package org.dominik.pass.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.data.dto.RefreshTokenDTO;
import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
  @NonNull private final SecurityUtils securityUtils;

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
      securityUtils.prepareForbiddenResponse(response, "Request method is not supported");
      return;
    }

    // get Authorization header, if header is null return FORBIDDEN response
    String header = request.getHeader(HEADER);

    if (header == null) {
      log.error("Authorization header is missing");
      SecurityContextHolder.clearContext();
      securityUtils.prepareForbiddenResponse(response, "Required header is missing");
      return;
    }

    // get token from header, if header is null return FORBIDDEN response
    String token = securityUtils.getToken(header);

    if (token == null || token.length() == 0) {
      log.error("Schema missing or invalid");
      SecurityContextHolder.clearContext();
      securityUtils.prepareForbiddenResponse(response, "Scheme missing or invalid");
      return;
    }

    try {
      String subject =  jwtUtils.readSubject(token, JwtUtils.TokenType.REFRESH_TOKEN);
      RefreshTokenDTO refreshTokenDTO = refreshTokenService.findByToken(token);

      if (refreshTokenDTO.isUsed()) {
        // if refresh token has been used for the second time, delete all user tokens and return security message
        refreshTokenService.deleteAllAccountTokens(subject);
        SecurityContextHolder.clearContext();
        securityUtils.prepareForbiddenResponse(response, "Security Exception. Server detected that the same token has been used again");
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
    } catch (Exception ex) {
      securityUtils.prepareForbiddenResponse(response, "Token is not valid");
    }
  }
}
