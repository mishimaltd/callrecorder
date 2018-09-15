package com.mishima.callrecorder.callservice.service;

import static org.junit.Assert.assertEquals;

import com.mishima.callrecorder.app.Application;
import com.mishima.callrecorder.callservice.entity.Call;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class CallServiceTest {

  @Autowired
  private CallService callService;

  private List<Call> testCalls = Arrays.asList(
      Call.builder().accountId("123").trial(false).paid(false).recordingDuration(200).build(),
      Call.builder().accountId("123").trial(false).paid(false).recordingDuration(100).build(),
      Call.builder().accountId("123").trial(false).paid(false).recordingDuration(500).build(),
      Call.builder().accountId("123").trial(false).paid(true).recordingDuration(100).build(),
      Call.builder().accountId("123").trial(true).paid(false).recordingDuration(100).build(),
      Call.builder().accountId("456").trial(false).paid(false).recordingDuration(150).build(),
      Call.builder().accountId("456").trial(false).paid(false).recordingDuration(250).build(),
      Call.builder().accountId("456").trial(false).paid(true).recordingDuration(250).build()
  );

  @Before
  public void setup() {
    testCalls.forEach(call -> callService.save(call));
  }

  @After
  public void teardown() {
    testCalls.forEach(call -> callService.deleteCallById(call.getId()));
  }

  @Test
  public void givenAccountThenExpectCalls() {
    assertEquals(5, callService.findByAccountId("123").size());
  }

  @Test
  public void givenAccountThenExpectUnpaidCalls() {
    Map<String,List<Call>> unpaidCallsByAccount = callService.findUnpaidCallsByAccountId();
    List<Call> callsForAccount = unpaidCallsByAccount.get("123");
    assertEquals(3, callsForAccount.size());
  }
}
