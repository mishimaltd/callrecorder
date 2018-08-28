package com.mishima.callrecorder.accountservice.client;

import com.mishima.callrecorder.accountservice.entity.Account;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
    URI requestUri = UriComponentsBuilder.fromUriString(uri + "/getAccountIdByPhoneNumber?phoneNumber="
        + encodeParameter(phoneNumber)).build(true).toUri();
    ResponseEntity<String> response = restTemplate.getForEntity(requestUri, String.class);
    if(response.getStatusCodeValue() == 200) {
      String accountId = response.getBody();
      log.info("Got account id {} for phone number {}", accountId, phoneNumber);
      return Optional.ofNullable(accountId);
    } else {
      log.error("Received error {} looking up account id for phone number {}", response.getStatusCode(), phoneNumber);
      return Optional.empty();
    }
  }

  @Override
  public Optional<Account> getAccountById(String accountId) {
    log.info("Looking up account by accountId {}", accountId);
    String url = uri + "/getAccountById?accountId={accountId}";
    ResponseEntity<Account> response = restTemplate.getForEntity(url, Account.class, accountId);
    if(response.getStatusCodeValue() == 200) {
      Account account = response.getBody();
      log.info("Got account {} for accountId {}", account, accountId);
      return Optional.ofNullable(account);
    } else {
      log.error("Received error {} looking up account for accountId {}", response.getStatusCode(), accountId);
      return Optional.empty();
    }
  }

  private String encodeParameter(String parameter) {
    try {
      return URLEncoder.encode(parameter, "utf-8");
    } catch( UnsupportedEncodingException ex ) {
      log.error("Unsupported encoding exception {}", ex);
      return parameter;
    }
  }

}
