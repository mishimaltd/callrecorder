package com.mishima.callrecorder.callservice.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.callservice.handler.EventHandler;
import com.mishima.callrecorder.domain.entity.Event;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SQSListener implements MessageListener {

  @Autowired
  private EventHandler eventHandler;

  private final ObjectMapper om = new ObjectMapper();

  public void onMessage(Message message) {
    log.info("Processing message {}", message);
    TextMessage textMessage = (TextMessage)message;
    try {
      String text = textMessage.getText();
      log.info("Received text {}", text);
      Event event = om.readValue(text, new TypeReference<Event>(){});
      eventHandler.handle(event);
    } catch(Exception ex) {
      log.error("Error occurred processing message -> {}", ex);
    }
  }


}
