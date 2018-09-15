package com.mishima.callrecorder.accountservice.persistence;

import com.mishima.callrecorder.accountservice.entity.Account;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account, String> {

  Optional<Account> findByUsernameIgnoreCase(String username);

  Optional<Account> findByPhoneNumbers(String phoneNumber);

}
