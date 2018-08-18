package com.mishima.callrecorder.twiliocallhandler;

import com.mishima.callhandler.accountservice.client.AccountServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
public class TestApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

  @Bean
  @Primary
  public AccountServiceClient accountServiceClient() {
    return Mockito.mock(AccountServiceClient.class);
  }


}
