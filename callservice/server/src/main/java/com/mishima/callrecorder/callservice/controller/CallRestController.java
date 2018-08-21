package com.mishima.callrecorder.callservice.controller;

import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.persistence.CallRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class CallRestController {

  @Autowired
  private CallRepository callRepository;

  @ResponseBody
  @GetMapping(value = "/getCallBySid", produces = MediaType.APPLICATION_JSON_VALUE)
  public Call getCallBySid(@RequestParam("sid") String callSid) {
    log.info("Retrieving call by sid {}", callSid);
    Optional<Call> result = callRepository.findBySid(callSid);
    if(result.isPresent()) {
      Call call = result.get();
      log.info("Found call {} for sid {}", call, callSid);
      return call;
    } else {
      log.warn("No call found for sid {}", callSid);
      return null;
    }
  }

  @ResponseBody
  @PostMapping(value="/saveCall", produces = MediaType.APPLICATION_JSON_VALUE)
  public Call saveCall(@RequestBody Call call) {
    log.info("Saving call {}", call);
    callRepository.save(call);
    return call;
  }

}
