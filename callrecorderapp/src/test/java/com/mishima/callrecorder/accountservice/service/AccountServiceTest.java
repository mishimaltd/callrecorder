package com.mishima.callrecorder.accountservice.service;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.entity.CreateAccountRequest;
import com.mishima.callrecorder.accountservice.entity.CreateAccountResponse;
import com.mishima.callrecorder.app.Application;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class AccountServiceTest {

  @Autowired
  private AccountService accountService;

  private String accountId;

  @After
  public void teardown() {
    if( accountId != null ) {
      accountService.deleteAccountById(accountId);
    }
  }

  @Test
  public void givenValidCreateAccountRequestThenExpectAccount() {
    String username = "valid.user@email.com";
    CreateAccountRequest request = CreateAccountRequest.builder()
        .username(username)
        .password("123456")
        .phoneNumber("9195934467")
        .cardNumber("4012888888881881")
        .cardExpiry(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("MM/yy")))
        .cardCvc("777")
        .build();
    CreateAccountResponse response = accountService.createAccount(request);
    assertTrue(response.isSuccess());
    assertNotNull(response.getAccount());
    assertNotNull(response.getAccount().getStripeId());
    accountId = response.getAccount().getId();

    // Test find account by id
    Optional<Account> accountById = accountService.findById(accountId);
    assertTrue(accountById.isPresent());
    assertEquals(response.getAccount(), accountById.get());

    // Test find account by username
    Optional<Account> accountByUsername = accountService.findByUsername(username);
    assertTrue(accountByUsername.isPresent());
    assertEquals(response.getAccount(), accountByUsername.get());

    // Test accounts is non-empty
    assertTrue(accountService.findAll().iterator().hasNext());

  }


  @Test
  public void givenInvalidCreateAccountRequestThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder()
        .username("valid.user@email.com")
        .password("123456")
        .phoneNumber("9195934467")
        .cardNumber("4012888881")
        .cardExpiry(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("MM/yy")))
        .cardCvc("777")
        .build();
    CreateAccountResponse response = accountService.createAccount(request);
    assertFalse(response.isSuccess());
    assertTrue(response.getFieldErrors().containsKey("cardNumber"));
    assertTrue(response.getFieldErrors().get("cardNumber").contains("Card number is invalid"));
  }

  @Test
  public void givenInvalidIdThenExpectEmpty() {
    assertFalse(accountService.findById("dummyid").isPresent());
  }

  @Test
  public void givenInvalidUsernameThenExpectEmpty() {
    assertFalse(accountService.findByUsername("dummyid").isPresent());
  }

}
