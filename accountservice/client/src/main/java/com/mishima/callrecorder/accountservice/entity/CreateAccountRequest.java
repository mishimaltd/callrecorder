package com.mishima.callrecorder.accountservice.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {

  private String username;
  private String password;
  private String phoneNumber;
  private String cardNumber;
  private String cardExpiry;
  private String cardCvc;

  public CreateAccountRequest(String username, String password, String phoneNumber,
      String cardNumber, String cardExpiry, String cardCvc) {
    this.username = username;
    this.password = password;
    this.phoneNumber = phoneNumber;
    this.cardNumber = cardNumber;
    this.cardExpiry = cardExpiry;
    this.cardCvc = cardCvc;
  }

  private CreateAccountRequest() {

  }

  public static Builder builder() {
    return new Builder();
  }


  public static final class Builder {

    private String username;
    private String password;
    private String phoneNumber;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;

    private Builder() {
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder phoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
      return this;
    }


    public Builder cardNumber(String cardNumber) {
      this.cardNumber = cardNumber;
      return this;
    }

    public Builder cardExpiry(String cardExpiry) {
      this.cardExpiry = cardExpiry;
      return this;
    }

    public Builder cardCvc(String cardCvc) {
      this.cardCvc = cardCvc;
      return this;
    }

    public CreateAccountRequest build() {
      return new CreateAccountRequest(username, password, phoneNumber, cardNumber, cardExpiry, cardCvc);
    }
  }
}
