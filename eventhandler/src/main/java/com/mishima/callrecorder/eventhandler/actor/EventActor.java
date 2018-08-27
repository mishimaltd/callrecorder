package com.mishima.callrecorder.eventhandler.actor;

import akka.actor.AbstractActor;
import com.mishima.callrecorder.callservice.client.CallServiceClient;
import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.publisher.Publisher;
import com.mishima.callrecorder.publisher.entity.Command;
import com.mishima.callrecorder.publisher.entity.Command.CommandType;
import com.mishima.callrecorder.publisher.entity.Event;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventActor extends AbstractActor {

  private final CallServiceClient callServiceClient;
  private final Publisher publisher;
  private final String commandTopicArn;

  public EventActor(CallServiceClient callServiceClient,
      Publisher publisher, String commandTopicArn) {
    this.callServiceClient = callServiceClient;
    this.publisher = publisher;
    this.commandTopicArn = commandTopicArn;
  }

  public Receive createReceive() {
    return receiveBuilder()
        .match(Event.class, this::handle)
        .build();
  }

  private void handle(Event event) {
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
      case SMSNotificationSent:
        smsNotificationSent(event);
      case Error:
        error(event);
      default:
        log.info("Unknown publisher type {}", event.getEventType());
    }
  }

  private void createCall(Event event) {
    long now = System.currentTimeMillis();
    String callSid = event.getCallSid();
    Call call = Call.builder()
        .sid(callSid)
        .accountId((String)event.getAttributes().get("AccountId"))
        .status("Created")
        .trial((Boolean)event.getAttributes().get("Trial"))
        .from((String)event.getAttributes().get("From"))
        .created(now)
        .lastUpdated(now)
        .build();
    callServiceClient.saveCall(call);
    log.info("Saved new call with sid {}", callSid);
  }

  private void endCall(Event event) {
    String callSid = event.getCallSid();
    Optional<Call> result = callServiceClient.findByCallSid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      call.setDuration(Integer.valueOf(event.getAttributes().get("CallDuration").toString()));
      call.setLastUpdated(System.currentTimeMillis());
      callServiceClient.saveCall(call);
      log.info("Marked call sid {} as completed", callSid);
    } else {
      log.error("Error occurred ending call, could not find call by sid {}", callSid);
    }
  }

  private void recordingCompleted(Event event) {
    String callSid = event.getCallSid();
    String recordingSid = (String)event.getAttributes().get("RecordingSid");
    String recordingUrl = (String)event.getAttributes().get("RecordingUrl");
    Integer recordingDuration = (Integer)event.getAttributes().get("RecordingDuration");
    Optional<Call> result = callServiceClient.findByCallSid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      call.setStatus("RecordingComplete");
      call.setRecordingUrl(recordingUrl);
      call.setRecordingDuration(recordingDuration);
      call.setLastUpdated(System.currentTimeMillis());
      callServiceClient.saveCall(call);
      log.info("Marked call sid {} as recording complete", callSid);
      publisher.publish(commandTopicArn,
          Command.builder()
              .commandType(CommandType.UploadRecording)
              .callSid(callSid)
              .attribute("RecordingSid", recordingSid)
              .attribute("RecordingUrl", recordingUrl)
              .build());
    } else {
      log.error("Error occurred marking recording complete, could not find call by sid {}", callSid);
    }
  }

  private void recordingUploaded(Event event) {
    String callSid = event.getCallSid();
    String s3FileKey = (String)event.getAttributes().get("S3FileKey");
    Optional<Call> result = callServiceClient.findByCallSid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      call.setStatus("RecordingUploaded");
      call.setS3recordingUrl(s3FileKey);
      call.setLastUpdated(System.currentTimeMillis());
      callServiceClient.saveCall(call);
      log.info("Marked call sid {} as recording uploaded", callSid);
      CommandType commandType;
      if(call.isTrial()) {
        log.info("Call sid {} is a trial, will send SendRecordingSMS command");
        commandType = CommandType.SendRecordingSMS;
      } else {
        log.info("Call sid {} is not a trial, will send Billing command");
        commandType = CommandType.Billing;
      }
      publisher.publish(commandTopicArn,
          Command.builder()
              .commandType(commandType)
              .callSid(callSid)
              .build());
    } else {
      log.error("Error occurred marking recording complete, could not find call by sid {}", callSid);
    }
  }

  private void smsNotificationSent(Event event) {
    String callSid = event.getCallSid();
    Optional<Call> result = callServiceClient.findByCallSid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      call.setStatus("SMSNotificationSent");
      call.setLastUpdated(System.currentTimeMillis());
      callServiceClient.saveCall(call);
      log.info("Marked call sid {} as sms notification sent", callSid);
    } else {
      log.error("Error occurred marking recording complete, could not find call by sid {}", callSid);
    }
  }

  private void error(Event event) {
    String callSid = event.getCallSid();
    Optional<Call> result = callServiceClient.findByCallSid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      call.setStatus("Error");
      call.setLastUpdated(System.currentTimeMillis());
      callServiceClient.saveCall(call);
      log.info("Marked call sid {} as sms notification sent", callSid);
    } else {
      log.error("Error occurred marking recording complete, could not find call by sid {}", callSid);
    }
  }


}
