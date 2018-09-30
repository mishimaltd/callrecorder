package com.mishima.callrecorder.s3service.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
public class S3Service {

  private final AmazonS3 amazonS3;

  private final String bucketName;

  private final String redirectBucketName;

  public S3Service(AmazonS3 amazonS3, String bucketName, String redirectBucketName) {
    this.amazonS3 = amazonS3;
    this.bucketName = bucketName;
    this.redirectBucketName = redirectBucketName;
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
    //Generate long presigned url
    log.info("Generating pre-signed url for key {} with expiry date {}");
    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
        .withMethod(HttpMethod.GET)
        .withExpiration(expiry);
    URL url = amazonS3.generatePresignedUrl(request);
    log.info("Generated url {}", url);

    // Now generate a redirect to this key
    String shortKey = RandomStringUtils.random(10, true, true);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setHeader("x-amz-website-redirect-location", url.toString());
    PutObjectRequest putObjectRequest = new PutObjectRequest(redirectBucketName, shortKey,
        new InputStream() {
          @Override
          public int read() throws IOException {
            return -1;
          }
        }, metadata);
    amazonS3.putObject(putObjectRequest);

    // Now make the object public readable
    amazonS3.setObjectAcl(redirectBucketName, shortKey, CannedAccessControlList.PublicRead);

    // Generate and return short url
    String shortUrl = "http://callrecorder-redirect.s3-website-us-east-1.amazonaws.com/" + shortKey;
    log.info("Generated short url {}", shortUrl);
    return shortUrl;
  }


}