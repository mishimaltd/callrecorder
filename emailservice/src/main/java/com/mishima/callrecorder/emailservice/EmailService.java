package com.mishima.callrecorder.emailservice;

public interface EmailService {

  void sendResetPasswordLink(String emailAddress, String url);

  void sendRecordingLink(String emailAddress, String url);

}
