package com.mishima.callrecorder.stripe.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mishima.callrecorder.app.Application;
import com.mishima.callrecorder.stripe.client.response.CreateChargeResponse;
import com.mishima.callrecorder.stripe.client.response.CreateCustomerResponse;
import com.mishima.callrecorder.stripe.client.response.CreateTokenResponse;
import com.mishima.callrecorder.stripe.entity.CreditCard;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
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
public class StripeClientTest {

  @Autowired
  private StripeClient stripeClient;

  private String customerId;

  private final String emailAddress = "test.user@domain.com";

  @Before
  public void setup() {
    customerId = createTestCustomer().getId();
  }

  @After
  public void tearDown() {
    if( customerId != null ) {
      stripeClient.deleteCustomerById(customerId);
    }
  }

  @Test
  public void givenValidCardNumberThenReturnToken() {
    CreateTokenResponse response = stripeClient.createToken(createTestCreditCard());
    assertTrue(response.isSuccess());
    assertNotNull(response.getToken());
    assertEquals("4242", response.getToken().getCard().getLast4());
  }

  @Test
  public void givenValidCustomerIdThenReturnCustomer() {
    Optional<Customer> result = stripeClient.findCustomerById(customerId);
    assertTrue(result.isPresent());
    assertEquals(customerId, result.get().getId());
  }

  @Test
  public void givenValidChargeAmountThenReturnCharge() {
    String description = "Test charge for 10 minutes";
    CreateChargeResponse response = stripeClient.createCharge(customerId, 1000, emailAddress, description);
    assertTrue(response.isSuccess());
    Charge charge = response.getCharge();
    assertEquals(description, charge.getDescription());
  }

  @Test
  public void givenInvalidChargeAmountThenReturnCharge() {
    String description = "Test charge for 10 minutes";
    CreateChargeResponse response = stripeClient.createCharge(customerId, 10, emailAddress, description);
    assertFalse(response.isSuccess());
    assertEquals("Amount must be at least 50 cents", response.getErrorMessage());
  }

  private Customer createTestCustomer() {
    CreateTokenResponse tokenResponse = stripeClient.createToken(createTestCreditCard());
    assertTrue(tokenResponse.isSuccess());
    CreateCustomerResponse customerResponse = stripeClient.createCustomer(emailAddress, tokenResponse.getToken());
    assertTrue(customerResponse.isSuccess());
    return customerResponse.getCustomer();
  }

  private CreditCard createTestCreditCard() {
    return CreditCard.builder().number("4242424242424242").expiryMonth(12).expiryYear(2023).cvc(712).build();
  }


}
