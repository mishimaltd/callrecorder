package com.mishima.callrecorder.twiliocallhandler.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.util.Base64;
import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.app.Application;
import java.util.Collections;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc(secure = false)
public class TwilioRestControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Value("${twilio.baseUri}")
  private String baseUrl;

  @Value("${twilio.username}")
  private String username;

  @Value("${twilio.password}")
  private String password;

  private String accountPhoneNumber = "9195924466";

  @Autowired
  private AccountRepository accountRepository;

  private PasswordEncoder encoder = new BCryptPasswordEncoder();

  private Account account;

  @Before
  public void setup() {
    Optional<Account> result = accountRepository.findByPhoneNumbers(accountPhoneNumber);
    if(!result.isPresent()) {
      account = Account.builder()
          .username(username)
          .password(encoder.encode(password))
          .roles(Collections.singletonList("ROLE_TWILIO"))
          .phoneNumbers(Collections.singletonList(accountPhoneNumber))
          .build();
      accountRepository.save(account);
    }
  }

  @After
  public void teardown() {
    if( account != null ) {
      accountRepository.deleteById(account.getId());
    }
  }

  @Test
  public void givenIncomingCallThenReturnInstructions() throws Exception {
    mvc.perform(post("/api/twilio/receive")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("From",accountPhoneNumber))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
        .andExpect(content().xml(
            "<Response><Gather timeout=\"20\" action=\"" + baseUrl + "/confirm\" method=\"POST\"><Say voice=\"alice\">Please enter the number you wish to call on your keypad followed by the pound or hash key.</Say></Gather><Say voice=\"alice\">Sorry I didn't get any input, Goodbye!</Say></Response>"
        ));
  }

  @Test
  public void givenDigitsToCallThenReturnConfirmation() throws Exception {
    mvc.perform(post("/api/twilio/confirm")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("From", "54321")
        .param("Digits", "556677"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
        .andExpect(content().xml(
            "<Response><Gather timeout=\"20\" numDigits=\"1\" action=\"http://localhost:8080/api/confirmed\" method=\"POST\"><Say voice=\"alice\">I heard 5,5,6,6,7,7. Is that correct? Press 1 to confirm or any other key to try again.</Say></Gather><Say voice=\"alice\">Sorry I didn't get any input, Goodbye!</Say></Response>"
        ));
  }

  @Test
  public void givenNoDigitsToCallThenReturnRetry() throws Exception {
    mvc.perform(post("/api/twilio/confirm")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("From", "54321"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
        .andExpect(content().xml(
            "<Response><Gather timeout=\"20\" action=\"http://localhost:8080/api/confirm\" method=\"POST\"><Say voice=\"alice\">Sorry, I didn't get that. Please enter the number you wish to call on your keypad followed by the pound or hash key.</Say></Gather><Say voice=\"alice\">Sorry I didn't get any input, Goodbye!</Say></Response>"
        ));
  }


  @Test
  public void givenDigitsConfirmedThenReturnDial() throws Exception {
    String dialNumber = "9196658899";
    mvc.perform(post("/api/twilio/confirmed")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("From", accountPhoneNumber)
        .param("Digits", "1")
        .sessionAttr("Number", dialNumber)
        .sessionAttr("Trial", false))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
        .andExpect(content().xml(
            "<Response><Say voice=\"alice\">Connecting your call.</Say><Dial timeout=\"30\" callerId=\"9195924466\" record=\"record-from-answer\" recordingStatusCallback=\"http://localhost:8080/api/recording\" recordingStatusCallbackMethod=\"POST\"><Number>9196658899</Number></Dial></Response>"
        ));
  }

  @Test
  public void givenInvalidDigitsConfirmedThenReturnMessageAndInstructions() throws Exception {
    String dialNumber = "19009634465";
    mvc.perform(post("/api/twilio/confirmed")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("From", accountPhoneNumber)
        .param("Number", dialNumber)
        .param("Digits", "1"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
        .andExpect(content().xml(
            "<Response><Gather timeout=\"20\" action=\"http://localhost:8080/api/confirm\" method=\"POST\"><Say voice=\"alice\">I'm sorry, that appears to be an invalid number. Please note that only calls to domestic and non-premium numbers are supported. Please enter the number you wish to call on your keypad followed by the pound or hash key.</Say></Gather><Say voice=\"alice\">Sorry I didn't get any input, Goodbye!</Say></Response>"
        ));
  }

  @Test
  public void givenDigitsUnconfirmedThenReturnRetry() throws Exception {
    mvc.perform(post("/api/twilio/confirmed")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("From", "54321")
        .param("Number", "556677")
        .param("Digits", "2"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
        .andExpect(content().xml(
            "<Response><Gather timeout=\"20\" action=\"http://localhost:8080/api/confirm\" method=\"POST\"><Say voice=\"alice\">Please enter the number you wish to call on your keypad followed by the pound or hash key.</Say></Gather><Say voice=\"alice\">Sorry I didn't get any input, Goodbye!</Say></Response>"
        ));
  }

  @Test
  public void givenNoDigitsConfirmingNumberThenReturnRetry() throws Exception {
    mvc.perform(post("/api/twilio/confirmed")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("From", "54321")
        .param("Number", "556677"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
        .andExpect(content().xml(
            "<Response><Gather timeout=\"20\" action=\"http://localhost:8080/api/confirm\" method=\"POST\"><Say voice=\"alice\">Sorry, I didn't get that. Please enter the number you wish to call on your keypad followed by the pound or hash key.</Say></Gather><Say voice=\"alice\">Sorry I didn't get any input, Goodbye!</Say></Response>"
        ));
  }

  @Test
  public void givenRecordingThenReturnOK() throws Exception {
    mvc.perform(post("/api/twilio/recording")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("RecordingSid", "12345")
        .param("RecordingUrl", "http://localhost")
        .param("RecordingDuration", "100")
        .param("RecordingStatus", "complete"))
        .andExpect(status().isNoContent());
  }

  @Test
  public void givenCallCompletedThenReturnOK() throws Exception {
    mvc.perform(post("/api/twilio/completed")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("CallDuration", "20"))
        .andExpect(status().isNoContent());
  }

  private String getAuthHeader() {
    String token = username + ":" + password;
    String encoded = new String(Base64.encode(token.getBytes()));
    return "Basic " + encoded;
  }

}
