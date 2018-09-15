package com.mishima.callrecorder.accountservice.entity;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountResponse {

  private boolean success;
  private Account account;
  private Map<String,List<String>> fieldErrors;
  private List<String> globalErrors;

  public CreateAccountResponse(boolean success,
      Account account, Map<String, List<String>> fieldErrors, List<String> globalErrors) {
    this.success = success;
    this.account = account;
    this.fieldErrors = fieldErrors;
    this.globalErrors = globalErrors;
  }

  private CreateAccountResponse() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private boolean success;
    private Account account;
    private Map<String,List<String>> fieldErrors;
    private List<String> globalErrors;

    private Builder() {
    }

    public Builder success(boolean success) {
      this.success = success;
      return this;
    }

    public Builder account(Account account) {
      this.account = account;
      return this;
    }

    public Builder fieldErrors(Map<String, List<String>> fieldErrors) {
      this.fieldErrors = fieldErrors;
      return this;
    }

    public Builder globalErrors(List<String> globalErrors) {
      this.globalErrors = globalErrors;
      return this;
    }

    public CreateAccountResponse build() {
      return new CreateAccountResponse(success, account, fieldErrors, globalErrors);
    }
  }
}
