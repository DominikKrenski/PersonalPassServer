package org.dominik.pass.data.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public final class AuthDTO implements Serializable {
  @Serial private static final long serialVersionUID = 3L;

  private UUID publicId;
  private String salt;
  private String accessToken;
  private String refreshToken;
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
