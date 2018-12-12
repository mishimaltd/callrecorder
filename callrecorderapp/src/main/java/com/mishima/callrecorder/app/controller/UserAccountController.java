package com.mishima.callrecorder.app.controller;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.service.AccountService;
import com.mishima.callrecorder.accountservice.utils.CardType;
import com.mishima.callrecorder.callservice.entity.Call;
import com.mishima.callrecorder.callservice.service.CallService;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
public class UserAccountController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private CallService callService;

  @ResponseBody
  @GetMapping(value="/private/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  public ModelAndView profile() {
    Map<String,Object> model = newHashMap();
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    Optional<Account> result = accountService.findByUsername(username);
    if( result.isPresent()) {
      Account account = result.get();

      // Populate card details
      Map<String,Object> cardDetails = newHashMap();
      cardDetails.put("type", account.getCardType());
      cardDetails.put("name", CardType.getName(account.getCardType()));
      cardDetails.put("lastDigits", account.getLastFourDigitsOfCard());
      model.put("card", cardDetails);

      // Populate phone numbers
      model.put("phoneNumbers", account.getPhoneNumbers());

      // Populate current usage
      model.put("currentBalance", getAccountBalance(account.getId()));
    }
    return new ModelAndView("profile", model);
  }


  private double getAccountBalance(String accountId) {
    int balanceInCents = callService.findByAccountId(accountId).stream().filter(
        call -> !call.isTrial() && !call.isPaid()).mapToInt(Call::getCostInCents).sum();
    double balanceInDollars = (double)balanceInCents / 100d;
    log.info("Current account balance for accountId {} is ${}", accountId, balanceInDollars);
    return balanceInDollars;
  }


}
