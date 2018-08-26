package com.mishima.callrecorder.twiliocallhandler.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.util.Base64;
import com.mishima.callrecorder.accountservice.service.client.AccountServiceClient;
import com.mishima.callrecorder.twiliocallhandler.TestApplication;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
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

  @Autowired
  private AccountServiceClient accountServiceClient;

  private String accountPhoneNumber = "9195924466";
  private String accountId = "123456";

  @Before
  public void setup() {
    when(accountServiceClient.getAccountIdByPhoneNumber(any())).thenReturn(Optional.of(accountId));
  }

  @Test
  public void givenIncomingCallThenReturnInstructions() throws Exception {
    mvc.perform(post("/api/receive")
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
    mvc.perform(post("/api/confirm")
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
    mvc.perform(post("/api/confirm")
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
    mvc.perform(post("/api/confirmed")
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
    mvc.perform(post("/api/confirmed")
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
    mvc.perform(post("/api/confirmed")
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
    mvc.perform(post("/api/confirmed")
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
    mvc.perform(post("/api/recording")
        .header("Authorization", getAuthHeader())
        .param("CallSid", "12345")
        .param("RecordingUrl", "http://localhost")
        .param("RecordingStatus", "complete"))
        .andExpect(status().isOk());
  }

  private String getAuthHeader() {
    String token = username + ":" + password;
    String encoded = new String(Base64.encode(token.getBytes()));
    return "Basic " + encoded;
  }

}
