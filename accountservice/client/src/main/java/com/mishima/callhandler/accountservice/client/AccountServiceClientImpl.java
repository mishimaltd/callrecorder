package com.mishima.callhandler.accountservice.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class AccountServiceClientImpl implements AccountServiceClient {

  private final String uri;

  private final RestTemplate restTemplate = new RestTemplate();

  public AccountServiceClientImpl(String uri) {
    this.uri = uri;
  }

  @Override
  public Optional<String> getAccountIdByPhoneNumber(String phoneNumber) {
    log.info("Looking up account id for phone number {}", phoneNumber);
    String requestUrl = uri + "/getAccountIdByPhoneNumber?phoneNumber=" + phoneNumber;
    ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
    if(response.getStatusCodeValue() == 200) {
      String accountId = response.getBody();
      log.info("Got account id {} for phone number {}", accountId, phoneNumber);
      return Optional.ofNullable(accountId);
    } else {
      log.error("Received error {} looking up account id for phone number {}", response.getStatusCode());
      return Optional.empty();
    }
  }

}
