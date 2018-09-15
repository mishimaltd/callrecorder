package com.mishima.callrecorder.stripe.client.response;

import com.stripe.model.Charge;
import lombok.Getter;

@Getter
public class CreateChargeResponse {

  private final Charge charge;
  private final boolean success;
  private final String errorMessage;

  public CreateChargeResponse(Charge charge, boolean success, String errorMessage) {
    this.charge = charge;
    this.success = success;
    this.errorMessage = errorMessage;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Charge charge;
    private boolean success;
    private String errorMessage;

    private Builder() {
    }

    public Builder charge(Charge charge) {
      this.charge = charge;
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

    public CreateChargeResponse build() {
      return new CreateChargeResponse(charge, success, errorMessage);
    }
  }
}
