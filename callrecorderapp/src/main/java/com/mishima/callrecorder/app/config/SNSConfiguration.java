package com.mishima.callrecorder.app.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.mishima.callrecorder.publisher.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SNSConfiguration {

  @Value("${publishing.enabled}")
  private boolean enabled;

  private AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
        .withCredentials(new DefaultAWSCredentialsProviderChain())
        .withRegion(Regions.US_EAST_1)
        .build();

  @Bean
  public Publisher publisher() {
    return new Publisher(snsClient, enabled);
  }

}
