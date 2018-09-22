package com.mishima.callrecorder.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/public/home").setViewName("home");
    registry.addViewController("/public/welcome").setViewName("home");
    registry.addViewController("/").setViewName("home");
    registry.addViewController("/public/trial").setViewName("trial");
    registry.addViewController("/public/register").setViewName("register");
    registry.addViewController("/public/login").setViewName("login");
    registry.addViewController("/public/forgot").setViewName("forgot");
  }

}
