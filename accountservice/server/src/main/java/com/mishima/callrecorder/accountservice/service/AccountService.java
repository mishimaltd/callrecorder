package com.mishima.callrecorder.accountservice.service;

import com.mishima.callrecorder.accountservice.entity.Account;
import java.util.Optional;

public interface AccountService {

  Optional<Account> findById(String id);

  Optional<Account> findByUsername(String username);

  Optional<Account> findByPhoneNumbers(String phoneNumber);

}
