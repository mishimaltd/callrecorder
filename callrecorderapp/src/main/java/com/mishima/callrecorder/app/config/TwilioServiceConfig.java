package com.mishima.callrecorder.app.config;

import com.mishima.callrecorder.twilioservice.TwilioRecordingDeleterService;
import com.mishima.callrecorder.twilioservice.TwilioSMSService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioServiceConfig {

  @Value("${twilio.account.sid}")
  private String twilioAccountSid;

  @Value("${twilio.auth.token}")
  private String twilioAuthToken;

  @Value("${twilio.sms.from}")
  private String twilioSmsFrom;

  @Bean
  public TwilioSMSService twilioSMSService() {
    return new TwilioSMSService(twilioAccountSid, twilioAuthToken, twilioSmsFrom);
  }

  @Bean
  public TwilioRecordingDeleterService twilioRecordingDeleterService() {
    return new TwilioRecordingDeleterService(twilioAccountSid, twilioAuthToken);
  }


}
