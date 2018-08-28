package com.mishima.callrecorder.accountservice.service;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import java.util.Optional;

public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;

  public AccountServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Optional<Account> findById(String id) {
    return accountRepository.findById(id);
  }

  @Override
  public Optional<Account> findByUsername(String username) {
    return accountRepository.findByUsername(username);
  }

  @Override
  public Optional<Account> findByPhoneNumbers(String phoneNumber) {
    return accountRepository.findByPhoneNumbers(phoneNumber);
  }
}
