package com.mishima.callrecorder.event.publisher;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.event.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class EventPublisher {

  private final AmazonSNS amazonSNS;

  @Value("${event.topic.arn}")
  private final String topic;

  @Value("${event.publishing.enabled}")
  private final boolean enabled;

  public EventPublisher(AmazonSNS amazonSNS, String topic, boolean enabled) {
    this.amazonSNS = amazonSNS;
    this.topic = topic;
    this.enabled = enabled;
  }

  private final ObjectMapper om = new ObjectMapper();

  public void publish(Event event) {
    if(!enabled) return;
    try {
      String payload = om.writeValueAsString(event);
      PublishRequest publishRequest = new PublishRequest(topic, payload);
      PublishResult result = amazonSNS.publish(publishRequest);
      log.info("Published message id {}", result.getMessageId());
    } catch( Exception ex ) {
      log.error("Exception occurred publishing message -> {}", ex);
    }
  }

}
