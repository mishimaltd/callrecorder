package com.mishima.callrecorder.app.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.mishima.callrecorder.s3service.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3ServiceConfig {

  private AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withCredentials(new DefaultAWSCredentialsProviderChain())
        .withRegion(Regions.US_EAST_1)
        .withPayloadSigningEnabled(false)
        .build();

  @Value("${bucket.name}")
  private String bucket;

  @Bean
  public S3Service s3Service() {
    return new S3Service(s3Client, bucket);
  }

}
