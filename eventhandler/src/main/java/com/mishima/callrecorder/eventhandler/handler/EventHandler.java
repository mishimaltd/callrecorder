package com.mishima.callrecorder.eventhandler.handler;

import com.mishima.callrecorder.callservice.client.CallServiceClient;
import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.publisher.Publisher;
import com.mishima.callrecorder.publisher.entity.Command;
import com.mishima.callrecorder.publisher.entity.Command.CommandType;
import com.mishima.callrecorder.publisher.entity.Event;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventHandler {

  @Autowired
  private CallServiceClient callServiceClient;

  @Autowired
  private Publisher publisher;

  @Value("${command.topic.arn}")
  private String commandTopicArn;

  public void handle(Event event) {
    log.info("Handling publisher {}", event);
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
        log.info("Unknown publisher type {}", event.getEventType());
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
    callServiceClient.saveCall(call);
    log.info("Saved new call with sid {}", sid);
  }

  private void endCall(Event event) {
    String callSid = (String)event.getAttributes().get("CallSid");
    Optional<Call> result = callServiceClient.findByCallSid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      call.setStatus("Completed");
      call.setDuration(Integer.valueOf(event.getAttributes().get("Duration").toString()));
      call.setLastUpdated(System.currentTimeMillis());
      callServiceClient.saveCall(call);
      log.info("Marked call sid {} as completed", callSid);
    } else {
      log.error("Error occurred ending call, could not find call by sid {}", callSid);
    }
  }

  private void recordingCompleted(Event event) {
    String callSid = (String)event.getAttributes().get("CallSid");
    String recordingUrl = (String)event.getAttributes().get("RecordingUrl");
    Optional<Call> result = callServiceClient.findByCallSid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      call.setStatus("RecordingComplete");
      call.setRecordingUrl(recordingUrl);
      call.setLastUpdated(System.currentTimeMillis());
      callServiceClient.saveCall(call);
      log.info("Marked call sid {} as recording complete", callSid);
      publisher.publish(commandTopicArn,
          Command.builder()
              .commandType(CommandType.UploadRecording)
              .attribute("CallSid", callSid)
              .attribute("RecordingUrl", recordingUrl)
              .build());
    } else {
      log.error("Error occurred marking recording complete, could not find call by sid {}", callSid);
    }
  }

  private void recordingUploaded(Event event) {
    String callSid = (String)event.getAttributes().get("CallSid");
    String s3FileKey = (String)event.getAttributes().get("S3FileKey");
    Optional<Call> result = callServiceClient.findByCallSid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      call.setStatus("RecordingUploaded");
      call.setS3recordingUrl(s3FileKey);
      call.setLastUpdated(System.currentTimeMillis());
      callServiceClient.saveCall(call);
      log.info("Marked call sid {} as recording uploaded", callSid);
      publisher.publish(commandTopicArn,
          Command.builder()
              .commandType(CommandType.Billing)
              .attribute("CallSid", callSid)
              .build());
    } else {
      log.error("Error occurred marking recording complete, could not find call by sid {}", callSid);
    }
  }

}
