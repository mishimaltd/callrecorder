package com.mishima.callrecorder.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.entity.CreateAccountRequest;
import com.mishima.callrecorder.accountservice.entity.CreateAccountResponse;
import com.mishima.callrecorder.accountservice.service.AccountService;
import com.mishima.callrecorder.emailservice.EmailService;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/accountservice")
public class AccountRestController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private EmailService emailService;

  @Value("${callservice.uri}")
  private String baseUri;

  private final ObjectMapper om = new ObjectMapper();

  @ResponseBody
  @GetMapping(value = "/getAccountById", produces = MediaType.APPLICATION_JSON_VALUE)
  public Account getAccountById(@RequestParam("accountId") String accountId, HttpServletResponse res) {
    log.info("Looking up account by accountId {}", accountId);
    Optional<Account> result = accountService.findById(accountId);
    if(result.isPresent()) {
      Account account = result.get();
      log.info("Found account {} for accountId {}", account, accountId);
      return account;
    } else {
      log.warn("No account found for accountId {}", accountId);
      res.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }
  }

  @ResponseBody
  @GetMapping(value = "/getAccountByUsername", produces = MediaType.APPLICATION_JSON_VALUE)
  public Account getAccountByUsername(@RequestParam("username") String username, HttpServletResponse res) {
    log.info("Looking up account by username {}", username);
    Optional<Account> result = accountService.findByUsername(username);
    if(result.isPresent()) {
      Account account = result.get();
      log.info("Found account {} for username {}", account, username);
      return account;
    } else {
      log.warn("No account found for username {}", username);
      res.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }
  }

  @ResponseBody
  @GetMapping(value = "/getAccountIdByPhoneNumber", produces = MediaType.TEXT_PLAIN_VALUE)
  public String getAccountIdByPhoneNumber(@RequestParam("phoneNumber") String phoneNumber, HttpServletResponse res) {
    log.info("Looking up accountId by phone number {}", phoneNumber);
    Optional<Account> result = accountService.findByPhoneNumbers(phoneNumber);
    if(result.isPresent()) {
      Account account = result.get();
      log.info("Found account {} for phone number {}", account, phoneNumber);
      return account.getId();
    } else {
      log.warn("No account found for phone number {}", phoneNumber);
      res.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }
  }

  @ResponseBody
  @PostMapping(value = "/forgotPassword", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<byte[]> forgotPassword(@RequestParam("username") String username, HttpServletResponse response) throws Exception {
    log.info("Looking up account by username {}", username);
    Map<String,Object> model = new HashMap<>();
    Optional<Account> result = accountService.findByUsername(username);
    if(result.isPresent()) {
      Account account = result.get();
      log.info("Found account {} for username {}", account, username);
      // Generate a password reset hash
      String uuid = UUID.randomUUID().toString();
      account.setResetPasswordKey(uuid);
      account.setPasswordResetRequestTime(System.currentTimeMillis());
      accountService.save(account);
      // Send reset password link
      String resetLink = generateResetLink(username, uuid);
      emailService.sendResetPasswordLink(username, resetLink);
      model.put("success",true);
    } else {
      log.warn("No account found for username {}", username);
      model.put("success", false);
    }
    return response(model, HttpStatus.OK);
  }

  @ResponseBody
  @PostMapping(value="/registerAccount", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<byte[]> register(@RequestBody CreateAccountRequest request) throws Exception {
    log.info("Registering account {}", request);
    Map<String,Object> model = new HashMap<>();
    CreateAccountResponse response = accountService.createAccount(request);
    if( response.isSuccess()) {
      log.info("Account registered successfully!");
      model.put("success", true);
      model.put("account", response.getAccount());
      authenticateUser(request.getUsername()); // Authenticate directly after registration
      return response(model, HttpStatus.OK);
    } else {
      log.info("Account not registered! -> {}", response);
      model.put("success", false);
      model.put("fieldErrors", response.getFieldErrors());
      model.put("globalErrors", response.getGlobalErrors());
      return response(model, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @ResponseBody
  @GetMapping(value="/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Account> getAllAccounts() {
    log.info("Returning all accounts");
    return accountService.findAll();
  }

  @GetMapping(value="/delete", produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteAccount(@RequestParam("accountId") String accountId) {
    log.info("Deleting account with id {}");
    accountService.deleteAccountById(accountId);
  }


  private ResponseEntity<byte[]> response(Object model, HttpStatus status) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setCacheControl("no-cache");
    String content = om.writeValueAsString(model);
    return new ResponseEntity<>(content.getBytes(StandardCharsets.UTF_8), headers, status);
  }

  private void authenticateUser(String username) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),userDetails.getPassword(),userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private String generateResetLink(String username, String uuid) {
    String url = baseUri + "/accountservice/resetPassword?username=" + username + "&hash=" + uuid;
    try {
      String encoded = URLEncoder.encode(url, "utf-8");
      log.info("Generated password reset link -> {}", encoded);
      return encoded;
    } catch( UnsupportedEncodingException ex ) {
      log.error("Encoding exception -> {}", ex);
      return null;
    }

  }

}
