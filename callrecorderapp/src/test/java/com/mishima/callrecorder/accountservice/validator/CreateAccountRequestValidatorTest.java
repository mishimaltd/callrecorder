package com.mishima.callrecorder.accountservice.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.entity.CreateAccountRequest;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import com.mishima.callrecorder.app.Application;
import com.mishima.callrecorder.domain.validation.ValidationResult;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class CreateAccountRequestValidatorTest {

  private CreateAccountRequestValidator validator;

  @Autowired
  private AccountRepository accountRepository;

  private String username = "test.user@email.com";
  private List<String> phoneNumbers = Arrays.asList("11223344", "22334455");

  private Account account;

  @Before
  public void setup() {
    validator = new CreateAccountRequestValidator(accountRepository);
    Optional<Account> result = accountRepository.findByUsernameIgnoreCase(username);
    if(!result.isPresent()) {
      account = Account.builder().username(username).phoneNumbers(phoneNumbers).build();
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
  public void givenEmptyFieldsThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("username"));
    assertTrue(result.getFieldErrors().containsKey("password"));
    assertTrue(result.getFieldErrors().containsKey("phoneNumber"));
    assertTrue(result.getFieldErrors().containsKey("cardNumber"));
    assertTrue(result.getFieldErrors().containsKey("cardExpiry"));
    assertTrue(result.getFieldErrors().containsKey("cardCvc"));
  }

  @Test
  public void givenInvalidUsernameThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().username("invalid").build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("username"));
    assertTrue(result.getFieldErrors().get("username").contains("Username is invalid"));
  }

  @Test
  public void givenExistingUsernameThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().username(username).build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("username"));
    assertTrue(result.getFieldErrors().get("username").contains("Username already exists"));
  }

  @Test
  public void givenValidUsernameThenExpectSuccess() {
    CreateAccountRequest request = CreateAccountRequest.builder().username("valid.user@gmail.com").build();
    ValidationResult result = validator.validate(request);
    assertFalse(result.getFieldErrors().containsKey("username"));
  }

  @Test
  public void givenInvalidPasswordThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().password("1234").build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("password"));
    assertTrue(result.getFieldErrors().get("password").contains("Password must be at least 6 characters"));
  }

  @Test
  public void givenValidPasswordThenExpectSuccess() {
    CreateAccountRequest request = CreateAccountRequest.builder().password("123456").build();
    ValidationResult result = validator.validate(request);
    assertFalse(result.getFieldErrors().containsKey("password"));
  }

  @Test
  public void givenInvalidPhoneNumberThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().phoneNumber("1234").build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("phoneNumber"));
    assertTrue(result.getFieldErrors().get("phoneNumber").contains("Phone number is invalid"));
  }

  @Test
  public void givenInternationalPhoneNumberThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().phoneNumber("+447881626584").build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("phoneNumber"));
    assertTrue(result.getFieldErrors().get("phoneNumber").contains("Phone number is invalid"));
  }

  @Test
  public void givenValidPhoneNumberThenExpectSuccess() {
    CreateAccountRequest request = CreateAccountRequest.builder().phoneNumber("9195633325").build();
    ValidationResult result = validator.validate(request);
    assertFalse(result.getFieldErrors().containsKey("phoneNumber"));
  }

  @Test
  public void givenInvalidCardNumberThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().cardNumber("12341234").build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("cardNumber"));
    assertTrue(result.getFieldErrors().get("cardNumber").contains("Card number is invalid"));
  }

  @Test
  public void givenValidCardNumberThenExpectSuccess() {
    CreateAccountRequest request = CreateAccountRequest.builder().cardNumber("4012888888881881").build();
    ValidationResult result = validator.validate(request);
    assertFalse(result.getFieldErrors().containsKey("cardNumber"));
  }

  @Test
  public void givenInvalidExpiryThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().cardExpiry("18/2000").build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("cardExpiry"));
    assertTrue(result.getFieldErrors().get("cardExpiry").contains("Expiry date is invalid"));
  }

  @Test
  public void givenExpiredCardThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().cardExpiry("11/17").build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("cardExpiry"));
    assertTrue(result.getFieldErrors().get("cardExpiry").contains("Card has expired"));
  }

  @Test
  public void givenValidCardExpiryThenExpectSuccess() {
    LocalDate now = LocalDate.now();
    String cardExpiry = now.format(DateTimeFormatter.ofPattern("MM/yy"));
    CreateAccountRequest request = CreateAccountRequest.builder().cardExpiry(cardExpiry).build();
    ValidationResult result = validator.validate(request);
    assertFalse(result.getFieldErrors().containsKey("cardExpiry"));
  }

  @Test
  public void givenInvalidCardCvcThenExpectError() {
    CreateAccountRequest request = CreateAccountRequest.builder().cardCvc("12341234").build();
    ValidationResult result = validator.validate(request);
    assertTrue(result.getFieldErrors().containsKey("cardCvc"));
    assertTrue(result.getFieldErrors().get("cardCvc").contains("Cvc is invalid"));
  }

  @Test
  public void givenValidCardCvcThenExpectSuccess() {
    CreateAccountRequest request = CreateAccountRequest.builder().cardCvc("1234").build();
    ValidationResult result = validator.validate(request);
    assertFalse(result.getFieldErrors().containsKey("cardCvc"));
  }
}
