package org.dominik.pass.utils;

import lombok.NonNull;
import org.dominik.pass.configuration.EmailConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailTo;

import java.util.List;
import java.util.Properties;

@Component
public class EmailClient {

  @Autowired
  public EmailClient(EmailConfig emailConfig) {
    ApiClient client = Configuration.getDefaultApiClient();
    ApiKeyAuth apiKey = (ApiKeyAuth) client.getAuthentication("api-key");
    apiKey.setApiKey(emailConfig.getApiKey());
  }

  public CreateSmtpEmail sendHintEmail(@NonNull String email, String reminder) throws ApiException {
    SendSmtpEmailTo to = new SendSmtpEmailTo();
    to.email(email);

    Properties params = new Properties();
    params.setProperty("email", email);
    params.setProperty("reminder", reminder);

    TransactionalEmailsApi transactionalApi = new TransactionalEmailsApi();
    SendSmtpEmail smtpEmail = new SendSmtpEmail();
    smtpEmail.setTemplateId(1L);
    smtpEmail.to(List.of(to));
    smtpEmail.params(params);

    return transactionalApi.sendTransacEmail(smtpEmail);
  }
}
