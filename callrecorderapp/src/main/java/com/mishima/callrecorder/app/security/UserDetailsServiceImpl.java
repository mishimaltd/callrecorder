package com.mishima.callrecorder.app.security;

import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.persistence.AccountRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private AccountRepository accountRepository;

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<Account> result = accountRepository.findByUsernameIgnoreCase(username);
    if(!result.isPresent()) {
      throw new UsernameNotFoundException(username);
    } else {
      Account account = result.get();
      List<GrantedAuthority> roles = account.getRoles().stream().map(
          (Function<String, GrantedAuthority>) SimpleGrantedAuthority::new).collect(Collectors.toList());
      return new User(account.getUsername(), account.getPassword(), roles);
    }
  }

}
