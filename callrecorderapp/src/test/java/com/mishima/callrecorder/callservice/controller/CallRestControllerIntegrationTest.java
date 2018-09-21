package com.mishima.callrecorder.callservice.controller;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.mishima.callrecorder.app.config.SecurityConstants.EXPIRATION_TIME;
import static com.mishima.callrecorder.app.config.SecurityConstants.HEADER_STRING;
import static com.mishima.callrecorder.app.config.SecurityConstants.TOKEN_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.app.Application;
import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.persistence.CallRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Autowired
  private AccountRepository accountRepository;

  @Value("${secret.key}")
  private String secret;

  private Account account;

  private String username = "myusername";

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

    Optional<Account> accountResult = accountRepository.findByUsernameIgnoreCase(username);
    if(!accountResult.isPresent()) {
      account = Account.builder().username(username).build();
      accountRepository.save(account);
    }
  }

  @After
  public void teardown() {
    if( call != null ) {
      callRepository.deleteById(call.getId());
    }
    if( account != null ) {
      accountRepository.deleteById(account.getId());
    }
  }

  @Test
  public void givenExistingCallSidThenReturnCall() throws Exception {
    String json = mvc.perform(get("/api/callservice/getCallBySid")
        .header(HEADER_STRING, getAuthHeader())
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
    String json = mvc.perform(post("/api/callservice/saveCall")
        .header(HEADER_STRING, getAuthHeader())
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
    String json = mvc.perform(get("/api/callservice/getCallsByAccountId")
        .header(HEADER_STRING, getAuthHeader())
        .param("accountId", call.getAccountId()))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    List<Call> loaded = new ObjectMapper().readValue(json, new TypeReference<List<Call>>(){});
    assertEquals(call, loaded.get(0));
  }

  private String getAuthHeader() {
    String token = JWT.create()
        .withSubject(username)
        .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .sign(HMAC512(secret.getBytes()));
    return TOKEN_PREFIX + token;
  }
}
