package com.mishima.callrecorder.emailservice;

import com.mishima.callrecorder.callservice.entity.Call;

public interface EmailService {

  void sendNotification(String subject, String body);

  void sendResetPasswordLink(String emailAddress, String url);

  void sendRecordingLink(String emailAddress, Call call, String url);

}
