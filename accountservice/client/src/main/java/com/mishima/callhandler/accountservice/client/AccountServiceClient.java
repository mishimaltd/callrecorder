package com.mishima.callhandler.accountservice.client;

import java.util.Optional;

public interface AccountServiceClient {

  Optional<String> getAccountIdByPhoneNumber(String phoneNumber);

}
