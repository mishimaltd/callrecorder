package com.mishima.callhandler.accountservice.service.persistence;

import com.mishima.callhandler.accountservice.entity.Account;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account, String> {

  @Nullable
  Account findByUsername(String username);


  @Nullable
  @Query("{ 'phoneNumbers' : ?0 }")
  Account findByPhoneNumbers(String phoneNumber);

}
