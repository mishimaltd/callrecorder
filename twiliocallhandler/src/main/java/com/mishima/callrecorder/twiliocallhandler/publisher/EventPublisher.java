package com.mishima.callrecorder.twiliocallhandler.publisher;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.domain.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventPublisher {

  @Autowired
  private AmazonSNS amazonSNS;

  @Value("${event.topic.arn}")
  private String topic;

  @Value("${event.publishing.enabled}")
  private boolean enabled;

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
