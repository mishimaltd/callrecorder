package com.mishima.callrecorder.app.config;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)  throws IOException {
    response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
    response.addHeader("Content-Type","application/xml");
    response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setRealmName("Mishima");
    super.afterPropertiesSet();
  }

}
