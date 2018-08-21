package com.mishima.callrecorder.accountservice.service.persistence;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;

import com.mishima.callrecorder.accountservice.service.entity.Account;
import com.mishima.callrecorder.accountservice.service.Application;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AccountRepositoryTest {

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
  public void testFindAccountByUsername() {
    Account saved = accountRepository.findByUsername(username);
    assertNotNull(saved);
    assertEquals(account.getId(), saved.getId());
  }

  @Test
  public void testFindAccountByPhoneNumber() {
    for(String phoneNumber: phoneNumbers) {
      Account saved = accountRepository.findByPhoneNumbers(phoneNumber);
      assertNotNull(saved);
      assertEquals(account.getId(), saved.getId());
    }
  }

  @Test
  public void testFindAccountByInvalidPhoneNumber() {
    assertNull(accountRepository.findByPhoneNumbers(UUID.randomUUID().toString()));
  }


}

