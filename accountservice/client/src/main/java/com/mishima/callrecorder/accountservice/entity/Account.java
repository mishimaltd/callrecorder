package com.mishima.callrecorder.accountservice.entity;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Account {

  @Id
  private String id;
  @Indexed(unique = true)
  private String username;
  private List<String> phoneNumbers;

  private Account() {}

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String username;
    private List<String> phoneNumbers;

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder phoneNumbers(List<String> phoneNumbers) {
      this.phoneNumbers = phoneNumbers;
      return this;
    }

    public Account build() {
      Account account = new Account();
      account.setUsername(username);
      account.setPhoneNumbers(phoneNumbers);
      return account;
    }
  }

}
