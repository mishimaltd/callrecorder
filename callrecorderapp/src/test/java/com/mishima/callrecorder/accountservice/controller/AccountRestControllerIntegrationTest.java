package com.mishima.callrecorder.accountservice.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.app.Application;
import java.util.Arrays;
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
public class AccountRestControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private AccountRepository accountRepository;

  private String username = "myusername";
  private List<String> phoneNumbers = Arrays.asList("+11223344", "+22334455");

  private Account account;

  @Before
  public void setup() {
    Optional<Account> result = accountRepository.findByUsername(username);
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
  public void givenExistingAccountIdThenReturnAccount() throws Exception {
    String json = mvc.perform(get("/api/accountservice/getAccountById")
        .param("accountId", account.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    Account loaded = new ObjectMapper().readValue(json, new TypeReference<Account>(){});
    assertEquals(account, loaded);
  }
}
