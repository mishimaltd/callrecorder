package com.mishima.callrecorder.commandhandler.handler;

import com.mishima.callrecorder.publisher.Publisher;
import com.mishima.callrecorder.publisher.entity.Command;
import com.mishima.callrecorder.publisher.entity.Event;
import com.mishima.callrecorder.publisher.entity.Event.EventType;
import com.mishima.callrecorder.s3service.service.S3Service;
import java.io.InputStream;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CommandHandler {

  private static final int MAX_UPLOAD_RETRIES = 5;
  private static final int UPLOAD_RETRY_WAIT_MS = 5000;

  @Autowired
  private Publisher eventPublisher;

  @Value("${event.topic.arn}")
  private String eventTopicArn;

  @Autowired
  private S3Service s3Service;

  public void handle(Command command) {
    log.info("Handling command {}", command);
    switch(command.getCommandType()) {
      case UploadRecording:
        uploadRecording(command);
        break;
      case Billing:
        doBilling(command);
        break;
      default:
        log.info("Unknown command type {}", command.getCommandType());
    }
  }

  private void uploadRecording(Command command) {
      String callSid = (String)command.getAttributes().get("CallSid");
      String recordingUrl = (String)command.getAttributes().get("RecordingUrl");
      boolean uploaded = false;
      String message = null;
      int retries = 0;
      while(!uploaded && ++retries < MAX_UPLOAD_RETRIES) {
        try {
          log.info("Upload attempt {} for recording from url {}", retries, recordingUrl);
          URL url = new URL(recordingUrl);
          InputStream is = url.openStream();
          String fileKey = s3Service.upload(is, "audio/wav");
          uploaded = true;
          log.info("Uploaded recording from url {} to fileKey {}", recordingUrl, fileKey);

          // Delete from Twilio
          new RestTemplate().delete(recordingUrl + ".json");
          log.info("Deleted recording url {} from Twilio", recordingUrl);

          // Publish recording uploaded to s3
          eventPublisher.publish(eventTopicArn, Event.builder()
            .eventType(EventType.CallRecordingUploaded)
            .attribute("CallSid", callSid)
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
            .attribute("CallSid", callSid)
            .attribute("Message", message)
            .build());
      }
  }

  private void doBilling(Command command) {
  }

}
