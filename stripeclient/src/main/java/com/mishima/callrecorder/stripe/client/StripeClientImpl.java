package com.mishima.callrecorder.stripe.client;

import com.mishima.callrecorder.stripe.client.response.CreateChargeResponse;
import com.mishima.callrecorder.stripe.client.response.CreateCustomerResponse;
import com.mishima.callrecorder.stripe.client.response.CreateTokenResponse;
import com.mishima.callrecorder.stripe.entity.CreditCard;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Token;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StripeClientImpl implements StripeClient {

  public StripeClientImpl(String apiKey, String apiVersion) {
    Stripe.apiKey = apiKey;
    Stripe.apiVersion = apiVersion;
  }

  public CreateTokenResponse createToken(CreditCard creditCard) {
    log.info("Generating token for card {}", creditCard);
    Map<String,Object> tokenParams = new HashMap<>();
    Map<String,Object> cardParams = creditCard.toStripeParams();
    tokenParams.put("card", cardParams);
    try {
      Token token = Token.create(tokenParams);
      log.info("Generated token {}", token);
      return CreateTokenResponse.builder().success(true).token(token).build();
    } catch( StripeException ex ) {
      log.error("Exception occurred -> {}", ex);
      return CreateTokenResponse.builder().success(false).errorMessage(ex.getMessage()).build();
    }
  }

  @Override
  public CreateCustomerResponse createCustomer(String emailAddress, Token cardToken) {
    log.info("Creating new customer with email address {}", emailAddress);
    Map<String,Object> params = new HashMap<>();
    params.put("email", emailAddress);
    params.put("source", cardToken.getId());
    try {
      Customer customer = Customer.create(params);
      log.info("Generated customer {}", customer);
      return CreateCustomerResponse.builder().success(true).customer(customer).build();
    } catch( StripeException ex ) {
      log.error("Exception occurred -> {}", ex);
      return CreateCustomerResponse.builder().success(false).errorMessage(ex.getMessage()).build();
    }
  }

  @Override
  public Optional<Customer> findCustomerById(String customerId) {
    try {
      log.info("Looking up customer by id {}", customerId);
      Customer customer = Customer.retrieve(customerId);
      log.info("Found customer {}", customer);
      return Optional.of(customer);
    } catch( StripeException ex) {
      log.error("Could not find customer by id {} -> ", customerId, ex);
      return Optional.empty();
    }
  }

  @Override
  public void deleteCustomerById(String customerId) {
    log.info("Deleting customer with id {}", customerId);
    Optional<Customer> result = findCustomerById(customerId);
    if(result.isPresent()) {
      try {
        result.get().delete();
        log.info("Deleted customer with id {}", customerId);
      } catch( StripeException ex) {
        log.error("Could not delete customer by id {} -> ", customerId, ex);
      }
    }
  }


  @Override
  public CreateChargeResponse createCharge(String customerId, int amount, String emailAddress, String description) {
    Map<String,Object> chargeParams = new HashMap<>();
    chargeParams.put("customer", customerId);
    chargeParams.put("amount", amount);
    chargeParams.put("currency", "usd");
    chargeParams.put("capture", true);
    chargeParams.put("description", description);
    try {
      Charge charge = Charge.create(chargeParams);
      return CreateChargeResponse.builder().success(true).charge(charge).build();
    } catch( StripeException ex ) {
      log.error("Exception occurred creating charge -> {}", ex);
      return CreateChargeResponse.builder().success(false).errorMessage(ex.getMessage()).build();
    }
  }
}
