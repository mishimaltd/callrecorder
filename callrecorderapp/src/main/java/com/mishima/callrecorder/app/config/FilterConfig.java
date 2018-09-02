package com.mishima.callrecorder.app.config;

import com.mishima.callrecorder.app.filter.CORSFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean myFilter() {
    FilterRegistrationBean<CORSFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(corsFilter());
    registration.addUrlPatterns("/*");
    registration.setName("corsFilter");
    registration.setOrder(1);
    return registration;
  }

  @Bean
  public CORSFilter corsFilter() {
    return new CORSFilter();
  }
}
