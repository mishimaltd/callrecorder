package com.mishima.callrecorder.publisher;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Publisher {

  private final AmazonSNS amazonSNS;

  private final boolean enabled;

  public Publisher(AmazonSNS amazonSNS, boolean enabled) {
    this.amazonSNS = amazonSNS;
    this.enabled = enabled;
  }

  private final ObjectMapper om = new ObjectMapper();

  public void publish(String topicArn, Object message) {
    if(!enabled) return;
    try {
      String payload = om.writeValueAsString(message);
      PublishRequest publishRequest = new PublishRequest(topicArn, payload);
      PublishResult result = amazonSNS.publish(publishRequest);
      log.info("Published message id {}", result.getMessageId());
    } catch( Exception ex ) {
      log.error("Exception occurred publishing message -> {}", ex);
    }
  }

}
