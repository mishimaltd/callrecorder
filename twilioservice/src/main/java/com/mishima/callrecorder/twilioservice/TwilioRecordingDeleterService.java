package com.mishima.callrecorder.twilioservice;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Recording;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwilioRecordingDeleterService {

  private final String accountSid;
  private final String authToken;

  public TwilioRecordingDeleterService(String accountSid, String authToken) {
    this.accountSid = accountSid;
    this.authToken = authToken;
  }

  public void deleteRecording(String recordingId) {
    log.info("Deleting recording id {}", recordingId);
    Twilio.init(accountSid, authToken);
    boolean deleted = Recording.deleter(recordingId).delete();
    if(deleted) {
      log.info("Deleted recording {} successfully!", recordingId);
    } else {
      log.warn("Could not delete recording {}", recordingId);
    }
  }
}
