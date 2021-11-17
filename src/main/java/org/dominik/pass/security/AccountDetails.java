package org.dominik.pass.security;

import lombok.NonNull;
import lombok.ToString;
import org.dominik.pass.data.dto.AccountDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ToString
public final class AccountDetails implements UserDetails {
  private final String username;
  private final UUID publicId;
  private final String password;
  private final boolean accountNonExpired;
  private final boolean accountNonLocked;
  private final boolean credentialsNonExpired;
  private final boolean enabled;
  private final List<GrantedAuthority> authorities;

  private AccountDetails(
      @NonNull String username,
      @NonNull UUID publicId,
      @NonNull String password,
      boolean accountNonExpired,
      boolean accountNonLocked,
      boolean credentialsNonExpired,
      boolean enabled,
      List<GrantedAuthority> authorities
  ) {
    this.username = username;
    this.publicId = publicId;
    this.password = password;
    this.accountNonExpired = accountNonExpired;
    this.accountNonLocked = accountNonLocked;
    this.credentialsNonExpired = credentialsNonExpired;
    this.enabled = enabled;
    this.authorities = authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return accountNonExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public UUID getPublicId() {
    return publicId;
  }

  public static AccountDetails fromDTO(@NonNull AccountDTO dto) {
    return new AccountDetails(
        dto.getEmail(),
        dto.getPublicId(),
        dto.getPassword(),
        dto.isAccountNonExpired(),
        dto.isAccountNonLocked(),
        dto.isCredentialsNonExpired(),
        dto.isEnabled(),
        List.of(new SimpleGrantedAuthority(dto.getRole().toString()))
    );
  }
}
