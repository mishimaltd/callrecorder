package com.mishima.callrecorder.callservice.handler;

import com.mishima.callrecorder.callservice.entity.Command;
import com.mishima.callrecorder.callservice.entity.RecordingUploadCommand;
import com.mishima.callrecorder.callservice.service.S3Service;
import com.mishima.callrecorder.event.entity.Event;
import com.mishima.callrecorder.event.entity.Event.EventType;
import com.mishima.callrecorder.event.publisher.EventPublisher;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CommandHandler {

  private static final int MAX_UPLOAD_RETRIES = 5;
  private static final int UPLOAD_RETRY_WAIT_MS = 5000;

  @Autowired
  private EventPublisher eventPublisher;

  @Autowired
  private S3Service s3Service;

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  public void handle(Command command) {
    log.info("Handling command {}", command);
    if(command instanceof RecordingUploadCommand) {
      handle((RecordingUploadCommand)command);
    }
  }

  private void handle(RecordingUploadCommand recordingUploadCommand) {
    executorService.submit(() -> {
      String recordingUrl = recordingUploadCommand.getRecordingUrl();
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
          eventPublisher.publish(Event.builder()
            .eventType(EventType.CallRecordingUploaded)
            .attribute("CallSid", recordingUploadCommand.getCallSid())
            .attribute("RecordingUrl", recordingUrl)
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
        eventPublisher.publish(Event.builder()
            .eventType(EventType.Error)
            .attribute("CallSid", recordingUploadCommand.getCallSid())
            .attribute("Message", message)
            .build());
      }
    });
  }

}
