package com.mishima.callrecorder.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;

public class TwilioWebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private AuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable().antMatcher("/api/twilio/**")
        .authorizeRequests().anyRequest().hasRole("TWILIO")
        .and().httpBasic().authenticationEntryPoint(authenticationEntryPoint);
  }

}
