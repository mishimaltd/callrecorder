package com.mishima.callrecorder.stripe.client.response;

import com.stripe.model.Token;
import lombok.Getter;

@Getter
public class CreateTokenResponse {

  private final Token token;
  private final boolean success;
  private final String errorMessage;

  public CreateTokenResponse(Token token, boolean success, String errorMessage) {
    this.token = token;
    this.success = success;
    this.errorMessage = errorMessage;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Token token;
    private boolean success;
    private String errorMessage;

    private Builder() {
    }

    public Builder token(Token token) {
      this.token = token;
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

    public CreateTokenResponse build() {
      return new CreateTokenResponse(token, success, errorMessage);
    }
  }
}
