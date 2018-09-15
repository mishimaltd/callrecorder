package com.mishima.callrecorder.app.config;

import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.accountservice.service.AccountService;
import com.mishima.callrecorder.accountservice.service.AccountServiceImpl;
import com.mishima.callrecorder.accountservice.validator.CreateAccountRequestValidator;
import com.mishima.callrecorder.stripe.client.StripeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AccountServiceConfig {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private StripeClient stripeClient;

  @Bean
  public AccountService accountService() {
    CreateAccountRequestValidator validator = new CreateAccountRequestValidator(accountRepository);
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    return new AccountServiceImpl(accountRepository, stripeClient, encoder, validator);
  }

}
