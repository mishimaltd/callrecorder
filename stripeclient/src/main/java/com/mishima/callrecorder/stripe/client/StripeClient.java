package com.mishima.callrecorder.stripe.client;

import com.mishima.callrecorder.stripe.client.response.CreateChargeResponse;
import com.mishima.callrecorder.stripe.client.response.CreateCustomerResponse;
import com.mishima.callrecorder.stripe.client.response.CreateTokenResponse;
import com.mishima.callrecorder.stripe.entity.CreditCard;
import com.stripe.model.Customer;
import com.stripe.model.Token;
import java.util.Optional;

public interface StripeClient {

  CreateTokenResponse createToken(CreditCard creditCard);

  CreateCustomerResponse createCustomer(String emailAddress, Token cardToken);

  Optional<Customer> findCustomerById(String customerId);

  void deleteCustomerById(String customerId);

  CreateChargeResponse createCharge(String customerId, int amount, String emailAddress, String description);

}
