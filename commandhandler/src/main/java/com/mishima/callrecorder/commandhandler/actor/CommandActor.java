package com.mishima.callrecorder.commandhandler.actor;

import akka.actor.AbstractActor;
import com.mishima.callrecorder.callservice.client.CallServiceClient;
import com.mishima.callrecorder.callservice.entity.Call;
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
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandActor extends AbstractActor {

  private static final int MAX_UPLOAD_RETRIES = 5;
  private static final int UPLOAD_RETRY_WAIT_MS = 5000;

  private final Publisher eventPublisher;
  private final CallServiceClient callServiceClient;
  private final String eventTopicArn;
  private final S3Service s3Service;
  private final TwilioSMSService twilioSMSService;
  private final TwilioRecordingDeleterService twilioRecordingDeleterService;
  private final TinyUrlService tinyUrlService;
  private final String callServiceUri;

  public CommandActor(Publisher eventPublisher, CallServiceClient callServiceClient, String eventTopicArn,
      S3Service s3Service, TwilioSMSService twilioSMSService, TwilioRecordingDeleterService twilioRecordingDeleterService,
      TinyUrlService tinyUrlService, String callServiceUri) {
    this.eventPublisher = eventPublisher;
    this.callServiceClient = callServiceClient;
    this.eventTopicArn = eventTopicArn;
    this.s3Service = s3Service;
    this.twilioSMSService = twilioSMSService;
    this.twilioRecordingDeleterService = twilioRecordingDeleterService;
    this.tinyUrlService = tinyUrlService;
    this.callServiceUri = callServiceUri;
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
      case Billing:
        billing(command);
        break;
      case SendRecordingSMS:
        sendSms(command);
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

  private void sendSms(Command command) {
    String callSid = command.getCallSid();
    Optional<Call> result = callServiceClient.findByCallSid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      String from = call.getFrom();
      String s3RecordingUrl = call.getS3recordingUrl();
      String payload = tinyUrlService.shorten(callServiceUri + "/recording/" + s3RecordingUrl);
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

}
