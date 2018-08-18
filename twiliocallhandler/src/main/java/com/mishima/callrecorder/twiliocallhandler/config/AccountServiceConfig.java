package com.mishima.callrecorder.twiliocallhandler.config;

import com.mishima.callhandler.accountservice.client.AccountServiceClient;
import com.mishima.callhandler.accountservice.client.AccountServiceClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountServiceConfig {

  @Value("${accountservice.uri}")
  private String accountServiceUri;

  @Bean
  public AccountServiceClient accountServiceClient() {
    return new AccountServiceClientImpl(accountServiceUri);
  }

}
