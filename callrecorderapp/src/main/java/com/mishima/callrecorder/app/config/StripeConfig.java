package com.mishima.callrecorder.app.config;

import com.mishima.callrecorder.stripe.client.StripeClient;
import com.mishima.callrecorder.stripe.client.StripeClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

  @Value("${stripe.api.key}")
  private String stripeApiKey;

  @Value("${stripe.api.version}")
  private String stripeApiVersion;

  @Bean
  public StripeClient stripeClient() {
    return new StripeClientImpl(stripeApiKey, stripeApiVersion);
  }

}
