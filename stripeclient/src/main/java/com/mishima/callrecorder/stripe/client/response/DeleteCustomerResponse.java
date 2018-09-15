package com.mishima.callrecorder.stripe.client.response;

import lombok.Getter;

@Getter
public class DeleteCustomerResponse {

  private final boolean success;
  private final String errorMessage;

  public DeleteCustomerResponse(boolean success, String errorMessage) {
    this.success = success;
    this.errorMessage = errorMessage;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private boolean success;
    private String errorMessage;

    private Builder() {
    }

    public Builder success(boolean success) {
      this.success = success;
      return this;
    }

    public Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public DeleteCustomerResponse build() {
      return new DeleteCustomerResponse(success, errorMessage);
    }
  }
}
