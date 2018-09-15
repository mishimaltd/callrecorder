package com.mishima.callrecorder.accountservice.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.entity.CreateAccountRequest;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.app.Application;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
public class AccountRestControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private AccountRepository accountRepository;

  private List<String> phoneNumbers = Arrays.asList("+11223344", "+22334455");

  private Account account;
  private String accountId;

  @Before
  public void setup() {
    String username = "myusername";
    Optional<Account> result = accountRepository.findByUsernameIgnoreCase(username);
    if(!result.isPresent()) {
      account = Account.builder().username(username).phoneNumbers(phoneNumbers).build();
      accountRepository.save(account);
    }
  }

  @After
  public void teardown() {
    if( account != null ) {
      accountRepository.deleteById(account.getId());
    }
    if( accountId != null ) {
      accountRepository.deleteById(accountId);
    }
  }

  @Test
  public void givenExistingPhoneNumberThenReturnAccountId() throws Exception {
    for(String phoneNumber: phoneNumbers) {
      mvc.perform(get("/api/accountservice/getAccountIdByPhoneNumber")
          .param("phoneNumber", phoneNumber))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andExpect(content().string(account.getId()));
    }
  }

  @Test
  public void givenInvalidPhoneNumberThenReturnNotFound() throws Exception {
    mvc.perform(get("/api/accountservice/getAccountIdByPhoneNumber")
        .param("phoneNumber", "dummy"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void givenExistingAccountIdThenReturnAccount() throws Exception {
    String json = mvc.perform(get("/api/accountservice/getAccountById")
        .param("accountId", account.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    Account loaded = new ObjectMapper().readValue(json, new TypeReference<Account>(){});
    assertEquals(account, loaded);
  }

  @Test
  public void givenInvalidAccountIdThenReturnNotFound() throws Exception {
    mvc.perform(get("/api/accountservice/getAccountById")
        .param("accountId", "dummy"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void givenExistingUsernameThenReturnAccount() throws Exception {
    String json = mvc.perform(get("/api/accountservice/getAccountByUsername")
        .param("username", account.getUsername()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    Account loaded = new ObjectMapper().readValue(json, new TypeReference<Account>(){});
    assertEquals(account, loaded);
  }

  @Test
  public void givenInvalidUsernameThenReturnNotFound() throws Exception {
    mvc.perform(get("/api/accountservice/getAccountByUsername")
        .param("username", "dummy"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void givenAccountDeletedThenExpectFewerAccounts() throws Exception {
    List<Account> accountsBefore = getAllAccounts();
    assertFalse(accountsBefore.isEmpty());
    mvc.perform(get("/api/accountservice/delete")
        .param("accountId", account.getId()))
        .andExpect(status().isOk());
    List<Account> accountsAfter = getAllAccounts();
    assertTrue(accountsBefore.size() > accountsAfter.size());
  }

  @Test
  public void givenValidCreateAccountRequestThenExpectAccount() throws Exception {
    String username = "valid.user@email.com";
    CreateAccountRequest request = CreateAccountRequest.builder()
        .username(username)
        .password("123456")
        .phoneNumber("9195934467")
        .cardNumber("4012888888881881")
        .cardExpiry(
            LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("MM/yy")))
        .cardCvc("777")
        .build();
    String json = mvc.perform(post("/api/accountservice/registerAccount")
        .content(new ObjectMapper().writeValueAsString(request))
        .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    ObjectMapper om = new ObjectMapper();
    Map<String,Object> result = om.readValue(json, new TypeReference<Map<String,Object>>(){});
    assertTrue((Boolean)result.get("success"));
    String accountJson = om.writeValueAsString(result.get("account"));
    Account account = om.readValue(accountJson, new TypeReference<Account>(){});
    assertEquals(username, account.getUsername());
    accountId = account.getId();
  }

  @Test
  public void givenInvalidCreateAccountRequestThenExpectError() throws Exception {
    String username = "valid.user@email.com";
    CreateAccountRequest request = CreateAccountRequest.builder()
        .username(username)
        .password("123456")
        .phoneNumber("9195934467")
        .cardNumber("401288888")
        .cardExpiry(
            LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("MM/yy")))
        .cardCvc("777")
        .build();
    String json = mvc.perform(post("/api/accountservice/registerAccount")
        .content(new ObjectMapper().writeValueAsString(request))
        .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    ObjectMapper om = new ObjectMapper();
    Map<String,Object> result = om.readValue(json, new TypeReference<Map<String,Object>>(){});
    assertFalse((Boolean)result.get("success"));
    String fieldErrorsJson = om.writeValueAsString(result.get("fieldErrors"));
    Map<String,List<String>> fieldErrors = om.readValue(fieldErrorsJson, new TypeReference<Map<String,List<String>>>(){});
    assertTrue(fieldErrors.containsKey("cardNumber"));
    assertTrue(fieldErrors.get("cardNumber").contains("Card number is invalid"));
  }


  private List<Account> getAllAccounts() throws Exception {
    String json = mvc.perform(get("/api/accountservice/accounts"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    return new ObjectMapper().readValue(json, new TypeReference<List<Account>>(){});
  }

}
