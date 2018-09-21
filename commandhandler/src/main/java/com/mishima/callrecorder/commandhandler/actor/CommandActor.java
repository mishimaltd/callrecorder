package com.mishima.callrecorder.commandhandler.actor;

import akka.actor.AbstractActor;
import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.service.CallService;
import com.mishima.callrecorder.commandhandler.tinyurl.TinyUrlService;
import com.mishima.callrecorder.publisher.Publisher;
import com.mishima.callrecorder.publisher.entity.Command;
import com.mishima.callrecorder.publisher.entity.Event;
import com.mishima.callrecorder.publisher.entity.Event.EventType;
import com.mishima.callrecorder.s3service.service.S3Service;
import com.mishima.callrecorder.twilioservice.TwilioRecordingDeleterService;
import com.mishima.callrecorder.twilioservice.TwilioSMSService;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandActor extends AbstractActor {

  private static final int MAX_UPLOAD_RETRIES = 5;
  private static final int UPLOAD_RETRY_WAIT_MS = 5000;

  private final Publisher eventPublisher;
  private final CallService callService;
  private final String eventTopicArn;
  private final S3Service s3Service;
  private final TwilioSMSService twilioSMSService;
  private final TwilioRecordingDeleterService twilioRecordingDeleterService;
  private final TinyUrlService tinyUrlService;

  public CommandActor(Publisher eventPublisher, CallService callService, String eventTopicArn,
      S3Service s3Service, TwilioSMSService twilioSMSService, TwilioRecordingDeleterService twilioRecordingDeleterService,
      TinyUrlService tinyUrlService) {
    this.eventPublisher = eventPublisher;
    this.callService = callService;
    this.eventTopicArn = eventTopicArn;
    this.s3Service = s3Service;
    this.twilioSMSService = twilioSMSService;
    this.twilioRecordingDeleterService = twilioRecordingDeleterService;
    this.tinyUrlService = tinyUrlService;
  }

  public Receive createReceive() {
    return receiveBuilder()
        .match(Command.class, this::handle)
        .build();
  }

  private void handle(Command command) {
    log.info("Handling command {}", command);
    switch(command.getCommandType()) {
      case UploadRecording:
        uploadRecording(command);
        break;
      case SendRecordingEmail:
        sendEmail(command);
        break;
      case SendRecordingSMS:
        sendSms(command);
        break;
      case Billing:
        billing(command);
        break;
      default:
        log.info("Unknown command type {}", command.getCommandType());
    }
  }

  private void uploadRecording(Command command) {
    String callSid = command.getCallSid();
    String recordingUrl = (String)command.getAttributes().get("RecordingUrl");
    String recordingSid = (String)command.getAttributes().get("RecordingSid");
    boolean uploaded = false;
    String message = null;
    int retries = 0;
    while(!uploaded && ++retries < MAX_UPLOAD_RETRIES) {
      try {
        log.info("Upload attempt {} for recording from url {}", retries, recordingUrl);
        URL url = new URL(recordingUrl + ".mp3"); // Download as mp3 not default wav format
        InputStream is = url.openStream();
        String fileKey = s3Service.upload(is, "audio/mpeg");
        uploaded = true;
        log.info("Uploaded recording from url {} to fileKey {}", recordingUrl, fileKey);

        // Delete from Twilio
        twilioRecordingDeleterService.deleteRecording(recordingSid);

        // Publish recording uploaded to s3 event
        eventPublisher.publish(eventTopicArn, Event.builder()
            .eventType(EventType.CallRecordingUploaded)
            .callSid(callSid)
            .attribute("S3FileKey", fileKey)
            .build());

      } catch( Exception ex ) {
        log.warn("Exception occurred processing recording {} -> {}, retrying in {}ms..", recordingUrl, ex, UPLOAD_RETRY_WAIT_MS);
        message = ex.getMessage();
        try {
          Thread.sleep(UPLOAD_RETRY_WAIT_MS);
        } catch( InterruptedException ignore) {
          // Ignore on purpose
        }
      }
    }

    if(!uploaded) {
      // Publish recording upload to s3 failed
      eventPublisher.publish(eventTopicArn, Event.builder()
          .eventType(EventType.Error)
          .callSid(callSid)
          .attribute("Message", message)
          .build());
    }
  }

  private void billing(Command command) {
  }

  private void sendEmail(Command command) {
    String callSid = command.getCallSid();
    Optional<Call> result = callService.findBySid(callSid);
    if(result.isPresent()) {

    } else {
      log.error("Could not find call with sid {}", callSid);
      // Publish recording upload to s3 failed
      eventPublisher.publish(eventTopicArn, Event.builder()
          .eventType(EventType.Error)
          .callSid(callSid)
          .attribute("Message", "Unable to send Email to caller, could not find call")
          .build());
    }
  }

  private void sendSms(Command command) {
    String callSid = command.getCallSid();
    Optional<Call> result = callService.findBySid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      String from = call.getFrom();
      String payload = generatePayload(call.getS3recordingUrl());
      String messageSid = twilioSMSService.sendMessage(from, payload);
      eventPublisher.publish(eventTopicArn, Event.builder()
          .eventType(EventType.SMSNotificationSent)
          .callSid(callSid)
          .attribute("MessageSid", messageSid)
          .build());
    } else {
      log.error("Could not find call with sid {}", callSid);
      // Publish recording upload to s3 failed
      eventPublisher.publish(eventTopicArn, Event.builder()
          .eventType(EventType.Error)
          .callSid(callSid)
          .attribute("Message", "Unable to send SMS to caller, could not find call")
          .build());
    }
  }

  private String generatePayload(String s3FileKey) {
    String preSignedUrl = s3Service.getPresignedUrl(s3FileKey, Date.from(LocalDateTime.now().plusDays(7).atZone(
        ZoneId.systemDefault()).toInstant()));
    String shortenedUrl = tinyUrlService.shorten(preSignedUrl);
    return "Thanks for trying out our service! Click the link below to access your recording:\n\n" + shortenedUrl;
  }

}
