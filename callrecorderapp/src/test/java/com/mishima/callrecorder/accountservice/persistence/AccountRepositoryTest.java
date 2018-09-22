package com.mishima.callrecorder.accountservice.persistence;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.app.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  }

  @Test
  public void testFindAccountByUsername() {
    Optional<Account> saved = accountRepository.findByUsernameIgnoreCase(username);
    assertTrue(saved.isPresent());
    assertEquals(account.getId(), saved.get().getId());
  }

  @Test
  public void testFindAccountByPhoneNumber() {
    for(String phoneNumber: phoneNumbers) {
      Optional<Account> saved = accountRepository.findByPhoneNumbers(phoneNumber);
      assertTrue(saved.isPresent());
      assertEquals(account.getId(), saved.get().getId());
    }
  }

  @Test
  @Ignore
  public void testCreateTwilioAccount() {
    PasswordEncoder encoder = new BCryptPasswordEncoder();
    String password = encoder.encode("pmmcha0s");
    Account account = Account.builder().username("mishimaltd@gmail.com").password(password).phoneNumbers(Collections.singletonList("+19195927481")).roles(Collections.singletonList("ROLE_USER")).build();
    accountRepository.save(account);
  }

  @Test
  public void testFindAccountByInvalidPhoneNumber() {
    assertFalse(accountRepository.findByPhoneNumbers(UUID.randomUUID().toString()).isPresent());
  }

}

