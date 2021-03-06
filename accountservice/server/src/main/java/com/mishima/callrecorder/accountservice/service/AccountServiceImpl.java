package com.mishima.callrecorder.accountservice.service;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.entity.CreateAccountRequest;
import com.mishima.callrecorder.accountservice.entity.CreateAccountResponse;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.accountservice.utils.CardType;
import com.mishima.callrecorder.domain.validation.EntityValidator;
import com.mishima.callrecorder.domain.validation.ValidationResult;
import com.mishima.callrecorder.stripe.client.StripeClient;
import com.mishima.callrecorder.stripe.client.response.CreateCustomerResponse;
import com.mishima.callrecorder.stripe.client.response.CreateTokenResponse;
import com.mishima.callrecorder.stripe.entity.CreditCard;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;

  private final StripeClient stripeClient;

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  private final EntityValidator<CreateAccountRequest> validator;

  public AccountServiceImpl(AccountRepository accountRepository, StripeClient stripeClient, BCryptPasswordEncoder bCryptPasswordEncoder, EntityValidator<CreateAccountRequest> validator) {
    this.accountRepository = accountRepository;
    this.stripeClient = stripeClient;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.validator = validator;
  }

  @Override
  public Account save(Account account) {
    return accountRepository.save(account);
  }

  @Override
  public Iterable<Account> findAll() {
    return accountRepository.findAll();
  }

  @Override
  public Optional<Account> findById(String id) {
    return accountRepository.findById(id);
  }

  @Override
  public Optional<Account> findByUsername(String username) {
    return accountRepository.findByUsernameIgnoreCase(username);
  }

  @Override
  public Optional<Account> findByPhoneNumbers(String phoneNumber) {
    return accountRepository.findByPhoneNumbers(phoneNumber);
  }

  @Override
  public CreateAccountResponse createAccount(CreateAccountRequest request) {
    ValidationResult validationResult = validator.validate(request);
    if(validationResult.isSuccess()) {
      CreateTokenResponse createTokenResponse = stripeClient.createToken(creditCard(request));
      if (createTokenResponse.isSuccess()) {
        CreateCustomerResponse createCustomerResponse = stripeClient.createCustomer(request.getUsername(), createTokenResponse.getToken());
        if(createCustomerResponse.isSuccess()) {
          Account account = accountRepository.save(account(request, createCustomerResponse.getCustomer().getId()));
          return CreateAccountResponse.builder().success(true).account(account).build();
        } else {
          return CreateAccountResponse.builder().success(false).globalErrors(
              Collections.singletonList(createCustomerResponse.getErrorMessage())).build();
        }
      } else {
        return CreateAccountResponse.builder().success(false).globalErrors(
            Collections.singletonList(createTokenResponse.getErrorMessage())).build();
      }
    }
    return CreateAccountResponse.builder().success(false).fieldErrors(validationResult.getFieldErrors()).build();
  }

  @Override
  public void deleteAccountById(String id) {
    Optional<Account> result = findById(id);
    if(result.isPresent()) {
      Account account = result.get();
      stripeClient.deleteCustomerById(account.getStripeId());
      accountRepository.deleteById(account.getId());
    }
  }

  @Override
  public boolean resetPassword(String username, String newPassword) {
    Optional<Account> result = findByUsername(username);
    if( result.isPresent()) {
      Account account = result.get();
      account.setPassword(bCryptPasswordEncoder.encode(newPassword));
      account.setResetPasswordKey(null); // Reset key to invalidate url
      save(account);
      return true;
    } else {
      log.error("Could not find account for username {}, will not reset password", username);
      return false;
    }
  }

  private CreditCard creditCard(CreateAccountRequest request) {
    return CreditCard.builder()
        .number(request.getCardNumber())
        .expiryMonth(Integer.valueOf(request.getCardExpiry().split("/")[0]))
        .expiryYear(Integer.valueOf(request.getCardExpiry().split("/")[1]))
        .cvc(Integer.valueOf(request.getCardCvc()))
        .build();
  }

  private Account account(CreateAccountRequest request, String stripeCustomerId) {
    return Account.builder()
        .username(request.getUsername())
        .password(bCryptPasswordEncoder.encode(request.getPassword()))
        .roles(Collections.singletonList("ROLE_USER"))
        .phoneNumbers(Collections.singletonList(request.getPhoneNumber()))
        .stripeId(stripeCustomerId)
        .lastFourDigitsOfCard(request.getCardNumber().substring(request.getCardNumber().length() -4 ))
        .cardType(CardType.detect(request.getCardNumber()).toString())
        .build();
  }

}
