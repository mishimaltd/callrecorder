package com.mishima.callrecorder.accountservice.client;

import com.mishima.callrecorder.accountservice.entity.Account;
import java.util.Optional;

public interface AccountServiceClient {

  Optional<String> getAccountIdByPhoneNumber(String phoneNumber);

  Optional<Account> getAccountById(String accountId);

}
