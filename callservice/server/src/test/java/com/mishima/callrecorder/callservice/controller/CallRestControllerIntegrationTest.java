package com.mishima.callrecorder.callservice.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.callservice.Application;
import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.persistence.CallRepository;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc(secure = false)
public class CallRestControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private CallRepository callRepository;

  private String callSid = "987654321";
  private String accountId = "334455";

  private Call call;

  @Before
  public void setup() {
    Optional<Call> result = callRepository.findBySid(callSid);
    if(!result.isPresent()) {
      call = Call.builder().sid(callSid).accountId(accountId).created(System.currentTimeMillis()).build();
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
  public void givenExistingCallSidThenReturnCall() throws Exception {
    String json = mvc.perform(get("/api/getCallBySid")
        .param("sid", call.getSid()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    Call loaded = new ObjectMapper().readValue(json, new TypeReference<Call>(){});
    assertEquals(call, loaded);
  }

  @Test
  public void givenExistingCallThenUpdateReturnsUpdatedCall() throws Exception {
    call.setStatus("updated");
    String json = mvc.perform(post("/api/saveCall")
        .content(new ObjectMapper().writeValueAsString(call))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    Call loaded = new ObjectMapper().readValue(json, new TypeReference<Call>(){});
    assertEquals("updated", loaded.getStatus());
  }

  @Test
  public void givenExistingAccountIdThenReturnCalls() throws Exception {
    String json = mvc.perform(get("/api/getCallsByAccountId")
        .param("accountId", call.getAccountId()))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    List<Call> loaded = new ObjectMapper().readValue(json, new TypeReference<List<Call>>(){});
    assertEquals(call, loaded.get(0));
  }
}
