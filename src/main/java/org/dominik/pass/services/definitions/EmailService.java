package org.dominik.pass.services.definitions;

public interface EmailService {
  String sendHint(String email);
  String sendTestEmail(String email);
}
