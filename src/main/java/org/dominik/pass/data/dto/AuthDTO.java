package org.dominik.pass.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Schema(
  description = "Class used to pass info about authentication to the client"
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public final class AuthDTO implements Serializable {
  @Serial private static final long serialVersionUID = 3L;

  @Schema(
    description = "Account's public id",
    name = "publicId",
    required = true,
    type = "string($uuid)"
  )
  private UUID publicId;

  @Schema(
    description = "Salt used to hash password",
    name = "salt",
    required = true,
    type = "string",
    minLength = 32,
    maxLength = 32,
    pattern = "^[a-fA-F0-9]{2,}$"
  )
  private String salt;

  @Schema(
    description = "Access token",
    name = "accessToken",
    required = true
  )
  private String accessToken;

  @Schema(
    description = "Refresh token",
    name = "refreshToken",
    required = true
  )
  private String refreshToken;

  @Schema(
    description = "Key used to encrypt master key",
    name = "key",
    required = true,
    minLength = 32,
    maxLength = 32,
    pattern = "^[a-fA-F0-9]{2,}$"
  )
  private String key;

  public static AuthBuilder builder() {
    return new AuthBuilder();
  }

  public static final class AuthBuilder {
    private UUID publicId;
    private String salt;
    private String key;
    private String accessToken;
    private String refreshToken;

    public AuthBuilder publicId(@NonNull UUID publicId) {
      this.publicId = publicId;
      return this;
    }

    public AuthBuilder salt(@NonNull String salt) {
      this.salt = salt;
      return this;
    }

    public AuthBuilder accessToken(@NonNull String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public AuthBuilder refreshToken(@NonNull String refreshToken) {
      this.refreshToken = refreshToken;
      return this;
    }

    public AuthBuilder key(@NonNull String key) {
      this.key = key;
      return this;
    }

    public AuthDTO build() {
      return new AuthDTO(publicId, salt, accessToken, refreshToken, key);
    }
  }
}
