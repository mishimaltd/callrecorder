package com.mishima.callrecorder.twiliocallhandler.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DialledNumberValidator {

  private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

  public boolean checkNumberIsValid(String phoneNumber) {
    log.info("Validating phone number {}", phoneNumber);
    try {
      PhoneNumber usNumberProto = phoneNumberUtil.parse(phoneNumber, "US");
      if(phoneNumberUtil.isValidNumberForRegion(usNumberProto, "US")) {
        PhoneNumberUtil.PhoneNumberType phoneNumberType = phoneNumberUtil.getNumberType(usNumberProto);
        if(phoneNumberType.equals(PhoneNumberType.PREMIUM_RATE)) {
          log.info("Phone number {} is a premium rate number", phoneNumber);
        } else {
          log.info("Phone number {} is valid!", phoneNumber);
          return true;
        }
      } else {
        log.info("Phone number {} is invalid for region US", phoneNumber);
      }
    } catch (NumberParseException ex) {
      log.error("Exception occurred parsing number {} -> {}", phoneNumber, ex);
    }
    return false;
  }



}
