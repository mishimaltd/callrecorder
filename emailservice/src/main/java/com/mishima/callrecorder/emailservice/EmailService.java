package com.mishima.callrecorder.emailservice;

public interface EmailService {

  void sendResetPasswordLink(String emailAddress, String resetUrl);

}
