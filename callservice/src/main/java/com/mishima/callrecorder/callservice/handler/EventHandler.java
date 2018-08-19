package com.mishima.callrecorder.callservice.handler;

import com.mishima.callrecorder.callservice.service.CallService;
import com.mishima.callrecorder.domain.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventHandler {

  @Autowired
  private CallService callService;

  public void handle(Event event) {
    log.info("Handling event {}", event);
  }

}
