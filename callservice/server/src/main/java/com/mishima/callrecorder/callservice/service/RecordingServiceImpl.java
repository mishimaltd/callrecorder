package com.mishima.callrecorder.callservice.service;

import com.mishima.callrecorder.s3service.service.S3Service;
import java.io.InputStream;

public class RecordingServiceImpl implements RecordingService {

  private final S3Service s3Service;

  public RecordingServiceImpl(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  @Override
  public InputStream download(String key) {
    return s3Service.download(key);
  }
}
