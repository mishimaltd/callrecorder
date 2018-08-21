package com.mishima.callrecorder.callservice.client;

import com.mishima.callrecorder.callservice.entity.Call;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class CallServiceClientImpl implements CallServiceClient {

  private final String uri;

  private final RestTemplate restTemplate = new RestTemplate();

  public CallServiceClientImpl(String uri) {
    this.uri = uri;
  }

  public Call saveCall(Call call) {
    log.info("Saving call {}", call);
    String url = uri + "/saveCall";
    ResponseEntity<Call> response = restTemplate.postForEntity(url, call, Call.class);
    return response.getBody();
  }

  public Optional<Call> findByCallSid(String callSid) {
    log.info("Looking up call for sid {}", callSid);
    String url = uri + "/getCallBySid?sid={callSid}";
    ResponseEntity<Call> response = restTemplate.getForEntity(url, Call.class, callSid);
    if(response.getStatusCodeValue() == 200) {
      Call call = response.getBody();
      log.info("Got call {} for callSid {}", call, callSid);
      return Optional.ofNullable(call);
    } else {
      log.error("Received error {} looking up call for callSid {}", response.getStatusCode(), callSid);
      return Optional.empty();
    }
  }



}
