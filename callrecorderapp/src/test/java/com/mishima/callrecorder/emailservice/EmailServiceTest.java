package com.mishima.callrecorder.emailservice;

import com.mishima.callrecorder.app.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class EmailServiceTest {

  @Autowired
  private EmailService emailService;

  @Test
  public void sendForgotPassword() throws Exception {
    emailService.sendResetPasswordLink("mishimaltd@gmail.com", "https://callrecorder-app.herokuapp.com/api/accountservice/newPassword?username=mishimaltd@gmail.com&token=53379108-cca6-40ab-8602-f8f484198faa");
    Thread.sleep(20000);
  }

}
