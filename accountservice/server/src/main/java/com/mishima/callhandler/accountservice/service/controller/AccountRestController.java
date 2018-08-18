package com.mishima.callhandler.accountservice.service.controller;

import com.mishima.callhandler.accountservice.entity.Account;
import com.mishima.callhandler.accountservice.service.persistence.AccountRepository;
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
@RequestMapping("/api")
public class AccountRestController {

  @Autowired
  private AccountRepository accountRepository;

  @ResponseBody
  @GetMapping(value = "/getAccountIdByPhoneNumber", produces = MediaType.TEXT_PLAIN_VALUE)
  public String getAccountIdByPhoneNumber(@RequestParam("phoneNumber") String phoneNumber) {
    log.info("Looking up accountId by phone number {}", phoneNumber);
    Account account = accountRepository.findByPhoneNumbers(phoneNumber);
    if(account != null ) {
      log.info("Found account {} for phone number {}", account, phoneNumber);
      return account.getId();
    } else {
      log.warn("No account found for phone number {}", phoneNumber);
      return null;
    }
  }

}
