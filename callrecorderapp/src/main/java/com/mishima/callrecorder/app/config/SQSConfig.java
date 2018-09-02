package com.mishima.callrecorder.app.config;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.mishima.callrecorder.commandhandler.listener.CommandListener;
import com.mishima.callrecorder.eventhandler.listener.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@Configuration
public class SQSConfig {

  @Value("${event.queue.name}")
  private String eventQueueName;

  @Value("${command.queue.name}")
  private String commandQueueName;

  @Autowired
  private EventListener eventListener;

  @Autowired
  private CommandListener commandListener;

  @SuppressWarnings("deprecated")
  SQSConnectionFactory connectionFactory =


      SQSConnectionFactory.builder()
          .withRegion(Region.getRegion(Regions.US_EAST_1))
          .withAWSCredentialsProvider(new DefaultAWSCredentialsProviderChain())
          .build();

  @Bean
  public DefaultMessageListenerContainer eventListenerContainer() {
    DefaultMessageListenerContainer dmlc = new DefaultMessageListenerContainer();
    dmlc.setConnectionFactory(connectionFactory);
    dmlc.setDestinationName(eventQueueName);
    dmlc.setMessageListener(eventListener);
    return dmlc;
  }

  @Bean
  public DefaultMessageListenerContainer commandListenerContainer() {
    DefaultMessageListenerContainer dmlc = new DefaultMessageListenerContainer();
    dmlc.setConnectionFactory(connectionFactory);
    dmlc.setDestinationName(commandQueueName);
    dmlc.setMessageListener(commandListener);
    return dmlc;
  }

}
