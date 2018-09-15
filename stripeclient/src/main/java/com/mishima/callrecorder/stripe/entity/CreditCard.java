package com.mishima.callrecorder.stripe.entity;

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class CreditCard {

  private final String number;
  private final int expiryMonth;
  private final int expiryYear;
  private final int cvc;

  private CreditCard(String number, int expiryMonth, int expiryYear, int cvc) {
    this.number = number;
    this.expiryMonth = expiryMonth;
    this.expiryYear = expiryYear;
    this.cvc = cvc;
  }

  public Map<String,Object> toStripeParams() {
    Map<String,Object> params = new HashMap<>();
    params.put("number", number);
    params.put("exp_month", expiryMonth);
    params.put("exp_year", expiryYear);
    params.put("cvc", cvc);
    return params;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String number;
    private int expiryMonth;
    private int expiryYear;
    private int cvc;

    private Builder() {
    }

    public Builder number(String number) {
      this.number = number;
      return this;
    }

    public Builder expiryMonth(int expiryMonth) {
      this.expiryMonth = expiryMonth;
      return this;
    }

    public Builder expiryYear(int expiryYear) {
      this.expiryYear = expiryYear;
      return this;
    }

    public Builder cvc(int cvc) {
      this.cvc = cvc;
      return this;
    }

    public CreditCard build() {
      return new CreditCard(number, expiryMonth, expiryYear, cvc);
    }
  }
}
