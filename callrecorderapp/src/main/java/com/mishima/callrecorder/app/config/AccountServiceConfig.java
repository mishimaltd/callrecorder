package com.mishima.callrecorder.app.config;

import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.accountservice.service.AccountService;
import com.mishima.callrecorder.accountservice.service.AccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountServiceConfig {

  @Autowired
  private AccountRepository accountRepository;

  @Bean
  public AccountService accountService() {
    return new AccountServiceImpl(accountRepository);
  }

}
