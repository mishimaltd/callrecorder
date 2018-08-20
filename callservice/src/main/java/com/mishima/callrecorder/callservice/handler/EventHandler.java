package com.mishima.callrecorder.callservice.handler;

import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.entity.RecordingUploadCommand;
import com.mishima.callrecorder.callservice.service.CallService;
import com.mishima.callrecorder.event.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventHandler {

  @Autowired
  private CallService callService;

  @Autowired
  private CommandHandler commandHandler;

  public void handle(Event event) {
    log.info("Handling event {}", event);
    switch(event.getEventType()) {
      case CallInitiated:
        createCall(event);
        break;
      case CallEnded:
        endCall(event);
        break;
      case CallRecordingCompleted:
        recordingCompleted(event);
        break;
      case CallRecordingUploaded:
        recordingUploaded(event);
        break;
      default:
        log.info("Unknown event type {}", event.getEventType());
    }
  }

  private void createCall(Event event) {
    long now = System.currentTimeMillis();
    String sid = (String)event.getAttributes().get("CallSid");
    Call call = Call.builder()
        .accountId((String)event.getAttributes().get("AccountId"))
        .status("Created")
        .sid(sid)
        .from((String)event.getAttributes().get("From"))
        .created(now)
        .lastUpdated(now)
        .build();
    callService.saveCall(call);
    log.info("Saved new call with sid {}", sid);
  }

  private void endCall(Event event) {
    String callSid = (String)event.getAttributes().get("CallSid");
    Call call = callService.findBySid(callSid);
    if(call != null) {
      call.setStatus("Completed");
      call.setDuration(Integer.valueOf(event.getAttributes().get("Duration").toString()));
      call.setLastUpdated(System.currentTimeMillis());
      callService.saveCall(call);
      log.info("Marked call sid {} as completed", callSid);
    } else {
      log.error("Error occurred ending call, could not find call by sid {}", callSid);
    }
  }

  private void recordingCompleted(Event event) {
    String callSid = (String)event.getAttributes().get("CallSid");
    String recordingUrl = (String)event.getAttributes().get("RecordingUrl");
    Call call = callService.findBySid(callSid);
    if(call != null) {
      call.setStatus("RecordingComplete");
      call.setRecordingUrl(recordingUrl);
      call.setLastUpdated(System.currentTimeMillis());
      callService.saveCall(call);
      log.info("Marked call sid {} as recording complete", callSid);
      commandHandler.handle(new RecordingUploadCommand(callSid, recordingUrl));
    } else {
      log.error("Error occurred marking recording complete, could not find call by sid {}", callSid);
    }
  }

  private void recordingUploaded(Event event) {
    String callSid = (String)event.getAttributes().get("CallSid");
    String s3FileKey = (String)event.getAttributes().get("S3FileKey");
    Call call = callService.findBySid(callSid);
    if(call != null) {
      call.setStatus("RecordingUploaded");
      call.setS3recordingUrl(s3FileKey);
      call.setLastUpdated(System.currentTimeMillis());
      callService.saveCall(call);
      log.info("Marked call sid {} as recording uploaded", callSid);
    } else {
      log.error("Error occurred marking recording complete, could not find call by sid {}", callSid);
    }
  }

}
