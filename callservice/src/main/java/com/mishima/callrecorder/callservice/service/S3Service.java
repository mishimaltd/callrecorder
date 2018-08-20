package com.mishima.callrecorder.callservice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class S3Service {

  @Autowired
  private AmazonS3 amazonS3;

  @Value("${s3.bucket}")
  private String bucketName;

  public String upload(InputStream is, String contentType) {
    String keyName = UUID.randomUUID().toString();
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    PutObjectRequest request = new PutObjectRequest(bucketName, keyName, is, metadata);
    amazonS3.putObject(request);
    return keyName;
  }

  public byte[] download(String fileKey) throws IOException {
    GetObjectRequest request = new GetObjectRequest(bucketName, fileKey);
    S3Object object = amazonS3.getObject(request);
    S3ObjectInputStream is = object.getObjectContent();
    return IOUtils.toByteArray(is);
  }

}