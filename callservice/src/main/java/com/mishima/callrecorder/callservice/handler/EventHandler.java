package com.mishima.callrecorder.callservice.handler;

import com.mishima.callrecorder.callservice.entity.Call;
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
    switch(event.getEventType()) {
      case CallInitiated:
        createCall(event);
        break;
      case CallEnded:
        endCall(event);
        break;
      default:
        log.info("Unknown event type {}", event.getEventType());
    }
  }

  private void createCall(Event event) {
    long now = System.currentTimeMillis();
    Call call = Call.builder()
        .accountId((String)event.getAttributes().get("AccountId"))
        .status("Created")
        .sid((String)event.getAttributes().get("CallSid"))
        .from((String)event.getAttributes().get("From"))
        .created(now)
        .lastUpdated(now)
        .build();
    callService.saveCall(call);
  }

  private void endCall(Event event) {
    String callSid = (String)event.getAttributes().get("CallSid");
    Call call = callService.findBySid(callSid);
    if(call != null) {
      call.setStatus("Completed");
      call.setDuration(Integer.valueOf(event.getAttributes().get("Duration").toString()));
      callService.saveCall(call);
    } else {
      log.error("Error occurred ending call, could not find call by sid {}", callSid);
    }
  }

}
