package com.mishima.callrecorder.app.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class CustomAuthenticationHandler implements AuthenticationSuccessHandler,
    AuthenticationFailureHandler {

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {
    if( authentication != null && authentication.isAuthenticated()) {
      response.setStatus(HttpStatus.OK.value());
    } else {
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception)  {
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }
}
