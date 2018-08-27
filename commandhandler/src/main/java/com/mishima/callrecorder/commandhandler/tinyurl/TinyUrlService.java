package com.mishima.callrecorder.commandhandler.tinyurl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class TinyUrlService {

  private final RestTemplate restTemplate = new RestTemplate();

  private final String baseUrl = "http://tinyurl.com/api-create.php?url={url}";

  public String shorten(String url) {
    log.info("Shortening url {}", url);
    ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class, url);
    if(response.getStatusCode().value() == 200) {
      String shortened = response.getBody();
      log.info("Url {} shortened to {}", url, shortened);
      return shortened;
    } else {
      log.warn("Error occurred shortening url {}, will return unshortened", url);
      return url;
    }
  }

}
