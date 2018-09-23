package com.mishima.callrecorder.app.config;

import com.mishima.callrecorder.app.thymeleaf.dialect.SecurityDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class MvcConfig implements WebMvcConfigurer {

  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("home");
    registry.addViewController("/public/home").setViewName("home");
    registry.addViewController("/public/welcome").setViewName("welcome");
    registry.addViewController("/public/trial").setViewName("trial");
    registry.addViewController("/public/register").setViewName("register");
    registry.addViewController("/public/login").setViewName("login");
    registry.addViewController("/public/forgot").setViewName("forgot");
    registry.addViewController("/public/reset").setViewName("reset");
    registry.addViewController("/private/account").setViewName("account");
  }

  @Bean
  public SecurityDialect customSecurityDialect() {
    return new SecurityDialect();
  }

}
