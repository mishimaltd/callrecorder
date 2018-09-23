package com.mishima.callrecorder.app.security;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationCheck {

  public boolean isAuthenticated() {
    SecurityContext context = SecurityContextHolder.getContext();
    if( context == null ) return false;
    return context.getAuthentication() != null;
  }

  public boolean isAnonymouys() {
    SecurityContext context = SecurityContextHolder.getContext();
    if( context == null ) return true;
    return context.getAuthentication() == null;
  }

}
