package com.mishima.callrecorder.accountservice.service.client;

import com.mishima.callrecorder.accountservice.service.entity.Account;
import java.util.Optional;

public interface AccountServiceClient {

  Optional<String> getAccountIdByPhoneNumber(String phoneNumber);

  Optional<Account> getAccountById(String accountId);

}
