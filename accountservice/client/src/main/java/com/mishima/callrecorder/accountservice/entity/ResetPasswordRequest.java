package com.mishima.callrecorder.accountservice.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

  private String password;

  public ResetPasswordRequest(String password) {
    this.password = password;
  }

  private ResetPasswordRequest() {
  }

}
