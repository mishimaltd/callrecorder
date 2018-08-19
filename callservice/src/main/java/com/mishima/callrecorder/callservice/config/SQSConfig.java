package com.mishima.callrecorder.callservice.config;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.mishima.callrecorder.callservice.messaging.SQSListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@Configuration
public class SQSConfig {

  @Value("${queue.name}")
  private String queueName;

  @Autowired
  private SQSListener sqsListener;

  @SuppressWarnings("deprecated")
  SQSConnectionFactory connectionFactory =
      SQSConnectionFactory.builder()
          .withRegion(Region.getRegion(Regions.US_EAST_1))
          .withAWSCredentialsProvider(new DefaultAWSCredentialsProviderChain())
          .build();

  @Bean
  public DefaultMessageListenerContainer jmsListenerContainer() {
    DefaultMessageListenerContainer dmlc = new DefaultMessageListenerContainer();
    dmlc.setConnectionFactory(connectionFactory);
    dmlc.setDestinationName(queueName);
    dmlc.setMessageListener(sqsListener);
    return dmlc;
  }


}
