package com.mishima.callrecorder.s3service.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
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
    log.info("Uploading file...");
    String key = UUID.randomUUID().toString();
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    PutObjectRequest request = new PutObjectRequest(bucketName, key, is, metadata);
    amazonS3.putObject(request);
    log.info("Uploaded file with key {}", key);
    return key;
  }

  public S3ObjectInputStream download(String key) {
    log.info("Downloading file with key {}", key);
    GetObjectRequest request = new GetObjectRequest(bucketName, key);
    S3Object object = amazonS3.getObject(request);
    return object.getObjectContent();
  }

  public String getPresignedUrl(String key, Date expiry) {
    log.info("Generating pre-signed url for key {} with expiry date {}");
    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
        .withMethod(HttpMethod.GET)
        .withExpiration(expiry);
    URL url = amazonS3.generatePresignedUrl(request);
    log.info("Generated url {}", url);
    return url.toString();
  }

}