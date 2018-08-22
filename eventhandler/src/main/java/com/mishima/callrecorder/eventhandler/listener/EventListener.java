package com.mishima.callrecorder.eventhandler.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.eventhandler.handler.EventHandler;
import com.mishima.callrecorder.publisher.entity.Event;
import java.util.Map;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventListener implements MessageListener {

  @Autowired
  private EventHandler eventHandler;

  private final ObjectMapper om = new ObjectMapper();

  private final TypeReference<Map<String,Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {};
  private final TypeReference<Event> eventTypeReference = new TypeReference<Event>() {};

  public void onMessage(Message message) {
    TextMessage textMessage = (TextMessage)message;
    try {
      String text = textMessage.getText();
      log.info("Received text {}", text);
      Map<String,Object> map = om.readValue(text, mapTypeReference);
      String body = (String)map.get("Message");
      Event event = om.readValue(body, eventTypeReference);
      eventHandler.handle(event);
    } catch(Exception ex) {
      log.error("Error occurred processing message -> {}", ex);
    }
  }

}
