package com.mishima.callrecorder.stripe.client.response;

import com.stripe.model.Customer;
import com.stripe.model.Token;
import lombok.Getter;

@Getter
public class CreateCustomerResponse {

  private final Customer customer;
  private final boolean success;
  private final String errorMessage;

  public CreateCustomerResponse(Customer customer, boolean success, String errorMessage) {
    this.customer = customer;
    this.success = success;
    this.errorMessage = errorMessage;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Customer customer;
    private boolean success;
    private String errorMessage;

    private Builder() {
    }

    public Builder customer(Customer customer) {
      this.customer = customer;
      return this;
    }

    public Builder success(boolean success) {
      this.success = success;
      return this;
    }

    public Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public CreateCustomerResponse build() {
      return new CreateCustomerResponse(customer, success, errorMessage);
    }
  }
}
