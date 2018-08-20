package com.mishima.callrecorder.callservice.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class RecordingUploadCommand implements Command {

  private final String callSid;
  private final String recordingUrl;

}
