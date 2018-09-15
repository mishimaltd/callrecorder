package com.mishima.callrecorder.accountservice.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.mishima.callrecorder.accountservice.entity.CreateAccountRequest;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.domain.validation.EntityValidator;
import com.mishima.callrecorder.domain.validation.ValidationResult;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.CreditCardValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.RegexValidator;
import org.springframework.util.StringUtils;

@Slf4j
public class CreateAccountRequestValidator implements EntityValidator<CreateAccountRequest> {

  private final AccountRepository accountRepository;

  private final EmailValidator emailValidator = EmailValidator.getInstance();

  private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

  private final CreditCardValidator creditCardValidator = new CreditCardValidator();

  private final RegexValidator cvcValidator = new RegexValidator("^[0-9]{3,4}");

  private final DateTimeFormatter expiryDateFormatter = DateTimeFormatter.ofPattern("MM/yy");

  public CreateAccountRequestValidator(
      AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public ValidationResult validate(CreateAccountRequest request) {
    ValidationResult.Builder builder = ValidationResult.builder();

    // Validate username
    String username = request.getUsername() == null? "": request.getUsername().trim();

    if(!StringUtils.hasText(username)) {
      builder.fieldError("username", "Username is required");
    } else {
      if(!emailValidator.isValid(username)) {
        builder.fieldError("username", "Username is invalid");
      }
      if(accountRepository.findByUsernameIgnoreCase(username).isPresent()) {
        builder.fieldError("username", "Username already exists");
      }
    }

    // Validate password
    String password = request.getPassword() == null? "": request.getPassword().trim();

    if(!StringUtils.hasText(password)) {
      builder.fieldError("password", "Password is required");
    } else {
      if(password.length() < 6) {
        builder.fieldError("password", "Password must be at least 6 characters");
      }
    }

    // Validate phone number
    String phoneNumber = request.getPhoneNumber() == null? "": request.getPhoneNumber().trim();

    if(!StringUtils.hasText(phoneNumber)) {
      builder.fieldError("phoneNumber", "Phone number is required");
    } else {
      try {
        PhoneNumber usNumberProto = phoneNumberUtil.parse(phoneNumber, "US");
        if(!phoneNumberUtil.isValidNumberForRegion(usNumberProto, "US")) {
          builder.fieldError("phoneNumber", "Phone number is invalid");
        }
      } catch(NumberParseException ex) {
        builder.fieldError("phoneNumber", "Phone number is invalid");
      }
    }

    // Validate credit card number
    String cardNumber = request.getCardNumber() == null? "": request.getCardNumber().trim();

    if(!StringUtils.hasText(cardNumber)) {
      builder.fieldError("cardNumber", "Card number is required");
    } else {
      if(!creditCardValidator.isValid(cardNumber)) {
        builder.fieldError("cardNumber", "Card number is invalid");
      }
    }

    // Validate expiry date
    String cardExpiry = request.getCardExpiry() == null? "": request.getCardExpiry().trim();

    if(!StringUtils.hasText(cardExpiry)) {
      builder.fieldError("cardExpiry", "Expiry date is required");
    } else {
      try {
        LocalDate expiryDate = YearMonth.parse(cardExpiry, expiryDateFormatter).atEndOfMonth();
        if(expiryDate.isBefore(LocalDate.now())) {
          builder.fieldError("cardExpiry", "Card has expired");
        }
      } catch( Exception ex ) {
        builder.fieldError("cardExpiry", "Expiry date is invalid");
      }
    }

    // Validate cvc
    String cardCvc = request.getCardCvc() == null? "": request.getCardCvc().trim();

    if(!StringUtils.hasText(cardCvc)) {
      builder.fieldError("cardCvc", "Cvc is required");
    } else {
      if(!cvcValidator.isValid(cardCvc)) {
        builder.fieldError("cardCvc", "Cvc is invalid");
      }
    }

    return builder.build();

  }


}
