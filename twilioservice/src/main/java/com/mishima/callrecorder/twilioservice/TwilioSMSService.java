package com.mishima.callrecorder.twilioservice;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwilioSMSService {

  private final String twilioAccountSid;
  private final String twilioAuthToken;
  private final String from;

  public TwilioSMSService(String twilioAccountSid, String twilioAuthToken, String from) {
    this.twilioAccountSid = twilioAccountSid;
    this.twilioAuthToken = twilioAuthToken;
    this.from = from;
  }

  public String sendMessage(String to, String payload) {
    Twilio.init(twilioAccountSid, twilioAuthToken);
    Message message = Message.creator(new PhoneNumber(to), new PhoneNumber(from), payload).create();
    log.info("Generated message with sid {}", message.getSid());
    return message.getSid();
  }

}
