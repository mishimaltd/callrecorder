package com.mishima.callrecorder.accountservice.service;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.entity.CreateAccountRequest;
import com.mishima.callrecorder.accountservice.entity.CreateAccountResponse;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.domain.validation.EntityValidator;
import com.mishima.callrecorder.domain.validation.ValidationResult;
import com.mishima.callrecorder.stripe.client.StripeClient;
import com.mishima.callrecorder.stripe.client.response.CreateTokenResponse;
import com.mishima.callrecorder.stripe.entity.CreditCard;
import java.util.Collections;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
        Account account = accountRepository.save(account(request, createTokenResponse.getToken().getId()));
        return CreateAccountResponse.builder().success(true).account(account).build();
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
        .build();
  }

}
