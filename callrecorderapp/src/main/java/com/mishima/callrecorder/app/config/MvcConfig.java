package com.mishima.callrecorder.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class MvcConfig implements WebMvcConfigurer {

  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("home");
    registry.addViewController("/public/login").setViewName("login");
    registry.addViewController("/public/forgot").setViewName("forgot");
    registry.addViewController("/public/home").setViewName("home");
    registry.addViewController("/public/welcome").setViewName("welcome");
    registry.addViewController("/public/trial").setViewName("trial");
    registry.addViewController("/public/register").setViewName("register");
    registry.addViewController("/public/forgot").setViewName("forgot");
    registry.addViewController("/public/reset").setViewName("reset");
    registry.addViewController("/public/error").setViewName("error");
    registry.addViewController("/private/account").setViewName("account");
    registry.setOrder(Ordered.LOWEST_PRECEDENCE);
  }

}
