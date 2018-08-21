package com.mishima.callrecorder.commandhandler.config;

import com.mishima.callrecorder.callservice.client.CallServiceClient;
import com.mishima.callrecorder.callservice.client.CallServiceClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CallServiceConfig {

  @Value("${callservice.uri}")
  private String callServiceUri;

  @Bean
  public CallServiceClient callServiceClient() {
    return new CallServiceClientImpl(callServiceUri);
  }

}
