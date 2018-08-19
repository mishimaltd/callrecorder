package com.mishima.callrecorder.callservice.persistence;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mishima.callrecorder.callservice.Application;
import com.mishima.callrecorder.callservice.entity.Call;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class CallRepositoryTest {

  @Autowired
  private CallRepository callRepository;

  private String callSid = "123445";

  private Call call;

  @Before
  public void setup() {
    call = callRepository.findBySid(callSid);
    if(call == null) {
      call = Call.builder().sid(callSid).build();
      callRepository.save(call);
    }
  }

  @After
  public void teardown() {
    if( call != null ) {
      callRepository.deleteById(call.getId());
    }
  }

  @Test
  public void testFindCallBySid() {
    Call saved = callRepository.findBySid(callSid);
    assertNotNull(saved);
    assertEquals(call.getId(), saved.getId());
  }

}