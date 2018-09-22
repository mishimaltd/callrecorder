package com.mishima.callrecorder.app.config;

import static com.mishima.callrecorder.app.config.SecurityConstants.SIGN_UP_URL;

import com.mishima.callrecorder.app.filter.JWTAuthenticationFilter;
import com.mishima.callrecorder.app.filter.JWTAuthorizationFilter;
import com.mishima.callrecorder.app.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${secret.key}")
  private String secret;

  @Autowired
  private AuthenticationEntryPoint authenticationEntryPoint;

  @Autowired
  private UserDetailsServiceImpl userDetailsService;

  private PasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable().authorizeRequests()
        .antMatchers("/", "/favicon.ico", "/public/*", SIGN_UP_URL).permitAll()
        .and()
        .antMatcher("/api/twilio/**").authorizeRequests().anyRequest().hasRole("TWILIO")
        .anyRequest().authenticated()
        .and().httpBasic().authenticationEntryPoint(authenticationEntryPoint)
        .and()
        .formLogin().loginPage("/public/login").permitAll()
        .and()
        .logout().logoutSuccessUrl("/").permitAll()
        .and()
        .addFilter(new JWTAuthenticationFilter(authenticationManager(), secret))
        .addFilter(new JWTAuthorizationFilter(authenticationManager(), secret))
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .rememberMe();
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(encoder);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
