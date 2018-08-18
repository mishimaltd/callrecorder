package com.mishima.callhandler.accountservice.service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mishima.callhandler.accountservice.entity.Account;
import com.mishima.callhandler.accountservice.service.Application;
import com.mishima.callhandler.accountservice.service.persistence.AccountRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(secure = false)
public class AccountRestControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private AccountRepository accountRepository;

  private String username = "myusername";
  private List<String> phoneNumbers = Arrays.asList("11223344", "22334455");

  private Account account;

  @Before
  public void setup() {
    account = accountRepository.findByUsername(username);
    if(account == null) {
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
      mvc.perform(get("/api/getAccountIdByPhoneNumber")
          .param("phoneNumber", phoneNumber))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andExpect(content().string(account.getId()));
    }

  }

}
