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
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Gather action=\"http://localhost:8080/api/confirm\" method=\"POST\" timeout=\"20\"><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/welcome.mp3</Play></Gather><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/no_input.mp3</Play></Response>"
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
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Gather action=\"http://localhost:8080/api/confirmed\" method=\"POST\" numDigits=\"1\" timeout=\"20\"><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/i_heard.mp3</Play><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/five.mp3</Play><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/five.mp3</Play><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/six.mp3</Play><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/six.mp3</Play><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/seven.mp3</Play><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/seven.mp3</Play></Gather><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/no_input.mp3</Play></Response>"
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
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Gather action=\"http://localhost:8080/api/confirm\" method=\"POST\" timeout=\"20\"><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/im_sorry.mp3</Play></Gather><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/no_input.mp3</Play></Response>"
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
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/connect.mp3</Play><Dial callerId=\"9195924466\" record=\"record-from-answer\" recordingStatusCallback=\"http://localhost:8080/api/recording\" recordingStatusCallbackMethod=\"POST\" timeout=\"30\"><Number>9196658899</Number></Dial></Response>"
        ));
  }

  @Test
  public void givenInvalidDigitsConfirmedThenReturnMessageAndInstructions() throws Exception {
    String dialNumber = "19009634465";
    mvc.perform(post("/api/twilio/confirmed").sessionAttr("Trial", false)
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("From", accountPhoneNumber)
        .param("Number", dialNumber)
        .param("Digits", "1"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
        .andExpect(content().xml(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Gather action=\"http://localhost:8080/api/confirm\" method=\"POST\" timeout=\"20\"><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/invalid_number.mp3</Play></Gather><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/no_input.mp3</Play></Response>"
        ));
  }

  @Test
  public void givenDigitsUnconfirmedThenReturnRetry() throws Exception {
    mvc.perform(post("/api/twilio/confirmed").sessionAttr("Trial", false)
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("From", "54321")
        .param("Number", "556677")
        .param("Digits", "2"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML))
        .andExpect(content().xml(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Gather action=\"http://localhost:8080/api/confirm\" method=\"POST\" timeout=\"20\"><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/welcome.mp3</Play></Gather><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/no_input.mp3</Play></Response>"
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
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Gather action=\"http://localhost:8080/api/confirm\" method=\"POST\" timeout=\"20\"><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/im_sorry.mp3</Play></Gather><Play loop=\"0\">https://s3.amazonaws.com/callrecorder-static/voice/no_input.mp3</Play></Response>"
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
