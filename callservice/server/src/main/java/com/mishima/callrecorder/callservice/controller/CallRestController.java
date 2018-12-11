package com.mishima.callrecorder.callservice.controller;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.service.AccountService;
import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.service.CallService;
import com.mishima.callrecorder.callservice.service.RecordingService;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/callservice")
public class CallRestController {

  @Autowired
  private CallService callService;

  @Autowired
  private AccountService accountService;

  @Autowired
  private RecordingService recordingService;

  private final ObjectMapper om = new ObjectMapper();


  @ResponseBody
  @GetMapping(value = "/calls", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<byte[]> getCallsForCurrentUser() throws Exception {
    Map<String,Object> model = newHashMap();
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    Optional<Account> account = accountService.findByUsername(username);
    if( account.isPresent()) {
      String accountId = account.get().getId();
      log.info("Retrieving calls by accountId {}", accountId);
      List<Call> calls = callService.findByAccountId(accountId);
      log.info("Found {} calls for accountId {}", calls.size(), accountId);
      model.put("data", calls);
    } else {
      log.error("Could not find account for username {}", username);
      model.put("data", Collections.emptyList());
    }
    return response(model, HttpStatus.OK);
  }


  @ResponseBody
  @GetMapping(value = "/getCallsByAccountId", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Call> getCallsByAccountId(@RequestParam("accountId") String accountId) {
    log.info("Retrieving calls by accountId {}", accountId);
    List<Call> calls = callService.findByAccountId(accountId);
    log.info("Found {} calls for accountId {}", calls.size(), accountId);
    return calls;
  }


  @ResponseBody
  @GetMapping(value = "/getCallBySid", produces = MediaType.APPLICATION_JSON_VALUE)
  public Call getCallBySid(@RequestParam("sid") String callSid) {
    log.info("Retrieving call by sid {}", callSid);
    Optional<Call> result = callService.findBySid(callSid);
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
    callService.save(call);
    return call;
  }

  @GetMapping("/recording/{sid}")
  public void download(@PathVariable("sid") String sid, HttpServletResponse res) throws Exception {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    Optional<Account> accountResult = accountService.findByUsername(username);
    if( accountResult.isPresent()) {
      Account account = accountResult.get();
      Optional<Call> callResult = callService.findBySid(sid);
      if(callResult.isPresent()) {
        Call call = callResult.get();
        if(call.getAccountId().equals(account.getId())) {
          String key = call.getS3recordingUrl();
          res.setHeader("Content-Disposition", "attachment; filename=\"recording.mpeg\"");
          res.setContentType("audio/mpeg");
          IOUtils.copy(recordingService.download(key), res.getOutputStream());
        } else {
          res.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized request for call " + sid);
        }
      } else {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find call with sid " + sid);
      }
    } else {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "No account found for username " + username);
    }
  }

  private ResponseEntity<byte[]> response(Object model, HttpStatus status) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setCacheControl("no-cache");
    String content = om.writeValueAsString(model);
    return new ResponseEntity<>(content.getBytes(StandardCharsets.UTF_8), headers, status);
  }
}
