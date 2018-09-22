package com.mishima.callrecorder.app.filter;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.mishima.callrecorder.app.config.SecurityConstants.EXPIRATION_TIME;
import static com.mishima.callrecorder.app.config.SecurityConstants.HEADER_STRING;
import static com.mishima.callrecorder.app.config.SecurityConstants.TOKEN_PREFIX;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.accountservice.entity.Account;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private AuthenticationManager authenticationManager;

  private String secret;

  private final ObjectMapper om = new ObjectMapper();

  public JWTAuthenticationFilter(AuthenticationManager authenticationManager, String secret) {
      this.authenticationManager = authenticationManager;
      this.secret = secret;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
    try {
      Account account = om.readValue(req.getInputStream(), Account.class);
      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              account.getUsername(),
              account.getPassword(),
              new ArrayList<>())
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth) {
    String token = JWT.create()
        .withSubject(((User) auth.getPrincipal()).getUsername())
        .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .sign(HMAC512(secret.getBytes()));
    res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
  }





}