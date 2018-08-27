package com.mishima.callrecorder.commandhandler.tinyurl;

import static org.junit.Assert.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class TinyUrlServiceIntegrationTest {

  private final TinyUrlService tinyUrlService = new TinyUrlService();

  @Test
  public void givenLongUrlThenReturnShortenedUrl() {
    String longUrl = "http://ec2-54-224-151-59.compute-1.amazonaws.com/api/receive?Trial=true";
    String shortened = tinyUrlService.shorten(longUrl);
    assertEquals("http://tinyurl.com/y7cumxs8", shortened);
  }

}
