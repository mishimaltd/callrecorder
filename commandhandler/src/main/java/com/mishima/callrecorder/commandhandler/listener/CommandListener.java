package com.mishima.callrecorder.commandhandler.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.commandhandler.handler.CommandHandler;
import com.mishima.callrecorder.publisher.entity.Command;
import java.util.Map;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandListener implements MessageListener {

  @Autowired
  private CommandHandler commandHandler;

  private final ObjectMapper om = new ObjectMapper();

  private final TypeReference<Map<String,Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {};
  private final TypeReference<Command> eventTypeReference = new TypeReference<Command>() {};

  public void onMessage(Message message) {
    TextMessage textMessage = (TextMessage)message;
    try {
      String text = textMessage.getText();
      log.info("Received text {}", text);
      Map<String,Object> map = om.readValue(text, mapTypeReference);
      String body = (String)map.get("Message");
      Command command = om.readValue(body, eventTypeReference);
      commandHandler.handle(command);
    } catch(Exception ex) {
      log.error("Error occurred processing message -> {}", ex);
    }
  }

}
