package com.mishima.callrecorder.accountservice.controller;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.service.AccountService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/accountservice")
public class AccountRestController {

  @Autowired
  private AccountService accountService;

  @ResponseBody
  @GetMapping(value = "/getAccountById", produces = MediaType.APPLICATION_JSON_VALUE)
  public Account getAccountById(@RequestParam("accountId") String accountId) {
    log.info("Looking up account by accountId {}", accountId);
    Optional<Account> result = accountService.findById(accountId);
    if(result.isPresent()) {
      Account account = result.get();
      log.info("Found account {} for accountId {}", account, accountId);
      return account;
    } else {
      log.warn("No account found for accountId {}", accountId);
      return null;
    }
  }


  @ResponseBody
  @GetMapping(value = "/getAccountIdByPhoneNumber", produces = MediaType.TEXT_PLAIN_VALUE)
  public String getAccountIdByPhoneNumber(@RequestParam("phoneNumber") String phoneNumber) {
    log.info("Looking up accountId by phone number {}", phoneNumber);
    Optional<Account> result = accountService.findByPhoneNumbers(phoneNumber);
    if(result.isPresent()) {
      Account account = result.get();
      log.info("Found account {} for phone number {}", account, phoneNumber);
      return account.getId();
    } else {
      log.warn("No account found for phone number {}", phoneNumber);
      return null;
    }
  }

}
