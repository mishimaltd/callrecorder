package com.mishima.callrecorder.app.config;

public class SecurityConstants {

  public static final long EXPIRATION_TIME = 864_000_000; // 10 days
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String HEADER_STRING = "Authorization";
  public static final String SIGN_UP_URL = "/api/accountservice/registerAccount";
  public static final String FORGOT_PASSWORD_URL = "/api/accountservice/forgotPassword";
  public static final String NEW_PASSWORD_URL = "/api/accountservice/newPassword";
  public static final String RESET_PASSWORD_URL = "/api/accountservice/resetPassword";

}
