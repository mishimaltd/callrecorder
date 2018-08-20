package com.mishima.callrecorder.callservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mishima.callrecorder.callservice.Application;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class S3UploaderTest {

  @Autowired
  private S3Service s3Service;

  @Test
  public void testUploadAndDownloadFile() throws Exception {
    String text = "small string";
    InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    String fileKey = s3Service.upload(is, "text/plain");
    log.info("Got file key {}", fileKey);
    assertNotNull(fileKey);
    byte[] content = s3Service.download(fileKey);
    String downloaded = new String(content, StandardCharsets.UTF_8);
    assertEquals(text, downloaded);
  }



}
