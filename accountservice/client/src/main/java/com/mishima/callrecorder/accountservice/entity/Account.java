package com.mishima.callrecorder.accountservice.entity;

import java.util.ArrayList;
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
  private String password;
  private String resetPasswordKey;
  private long passwordResetRequestTime;
  private List<String> roles;
  private List<String> phoneNumbers;
  private String stripeId;

  // Credit card details
  private String lastFourDigitsOfCard;
  private String cardType;

  private Account() {}

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String username;
    private String password;
    private String resetPasswordKey;
    private long passwordResetRequestTime;
    private List<String> roles = new ArrayList<>();
    private List<String> phoneNumbers;
    private String stripeId;
    private String lastFourDigitsOfCard;
    private String cardType;

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder resetPasswordKey(String resetPasswordKey) {
      this.resetPasswordKey = resetPasswordKey;
      return this;
    }

    public Builder passwordResetRequestTime(long passwordResetRequestTime) {
      this.passwordResetRequestTime = passwordResetRequestTime;
      return this;
    }

    public Builder roles(List<String> roles) {
      this.roles = roles;
      return this;
    }

    public Builder phoneNumbers(List<String> phoneNumbers) {
      this.phoneNumbers = phoneNumbers;
      return this;
    }

    public Builder stripeId(String stripeId) {
      this.stripeId = stripeId;
      return this;
    }

    public Builder lastFourDigitsOfCard(String lastFourDigitsOfCard) {
      this.lastFourDigitsOfCard = lastFourDigitsOfCard;
      return this;
    }

    public Builder cardType(String cardType) {
      this.cardType = cardType;
      return this;
    }

    public Account build() {
      Account account = new Account();
      account.setUsername(username);
      account.setPassword(password);
      account.setResetPasswordKey(resetPasswordKey);
      account.setPasswordResetRequestTime(passwordResetRequestTime);
      account.setRoles(roles);
      account.setPhoneNumbers(phoneNumbers);
      account.setStripeId(stripeId);
      account.setLastFourDigitsOfCard(lastFourDigitsOfCard);
      account.setCardType(cardType);
      return account;
    }
  }

}
