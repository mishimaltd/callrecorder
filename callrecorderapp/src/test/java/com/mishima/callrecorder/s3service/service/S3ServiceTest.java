package com.mishima.callrecorder.s3service.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class S3ServiceTest {

  private S3Service s3Service = new S3Service(s3Client(), "callrecorder-bucket", "callrecorder-redirect");

  @Test
  public void testUploadAndDownloadFile() throws Exception {
    String text = "small string";
    InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    String fileKey = s3Service.upload(is, "text/plain");
    log.info("Got file key {}", fileKey);
    assertNotNull(fileKey);
    byte[] content = IOUtils.toByteArray(s3Service.download(fileKey));
    String downloaded = new String(content, StandardCharsets.UTF_8);
    assertEquals(text, downloaded);

  }

  @Test
  public void testGenerateSignedUrl() throws Exception {
    String key = "48f544e0-1a19-4966-8735-aefabbdfea3e";
    Date date = Date.from(LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant());
    String preSignedUrl = s3Service.getPresignedUrl(key, date);
    assertNotNull(preSignedUrl);

    // Confirm that it can be accessed
    ResponseEntity<byte[]> response = new RestTemplate().getForEntity(new URI(preSignedUrl), byte[].class);
    assertEquals(HttpStatus.MOVED_PERMANENTLY, response.getStatusCode());
  }


  private AmazonS3 s3Client() {
    return AmazonS3ClientBuilder.standard()
        .withCredentials(new DefaultAWSCredentialsProviderChain())
        .withRegion(Regions.US_EAST_1)
        .withPayloadSigningEnabled(false)
        .build();
  }

}
