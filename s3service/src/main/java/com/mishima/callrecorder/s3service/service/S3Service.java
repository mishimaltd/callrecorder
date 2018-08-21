package com.mishima.callrecorder.s3service.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.InputStream;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3Service {

  private final AmazonS3 amazonS3;

  private final String bucketName;

  public S3Service(AmazonS3 amazonS3, String bucketName) {
    this.amazonS3 = amazonS3;
    this.bucketName = bucketName;
  }

  public String upload(InputStream is, String contentType) {
    String keyName = UUID.randomUUID().toString();
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    PutObjectRequest request = new PutObjectRequest(bucketName, keyName, is, metadata);
    amazonS3.putObject(request);
    return keyName;
  }

  public S3ObjectInputStream download(String fileKey) {
    GetObjectRequest request = new GetObjectRequest(bucketName, fileKey);
    S3Object object = amazonS3.getObject(request);
    return object.getObjectContent();
  }

}