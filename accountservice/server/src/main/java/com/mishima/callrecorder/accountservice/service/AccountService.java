package com.mishima.callrecorder.accountservice.service;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.entity.CreateAccountRequest;
import com.mishima.callrecorder.accountservice.entity.CreateAccountResponse;
import java.util.List;
import java.util.Optional;

public interface AccountService {

  Iterable<Account> findAll();

  Optional<Account> findById(String id);

  Optional<Account> findByUsername(String username);

  Optional<Account> findByPhoneNumbers(String phoneNumber);

  CreateAccountResponse createAccount(CreateAccountRequest request);

  void deleteAccountById(String id);

}
