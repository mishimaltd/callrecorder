package com.mishima.callrecorder.app.config;

import com.mishima.callrecorder.callservice.persistence.CallRepository;
import com.mishima.callrecorder.callservice.service.CallService;
import com.mishima.callrecorder.callservice.service.CallServiceImpl;
import com.mishima.callrecorder.callservice.service.RecordingService;
import com.mishima.callrecorder.callservice.service.RecordingServiceImpl;
import com.mishima.callrecorder.s3service.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class CallServiceConfig {

  @Autowired
  private CallRepository callRepository;

  @Autowired
  private S3Service s3Service;

  @Bean
  public CallService callService() {
    return new CallServiceImpl(callRepository);
  }

  @Bean
  public RecordingService recordingService() {
    return new RecordingServiceImpl(s3Service);
  }

}
