package com.mishima.callrecorder.emailservice;

import java.io.StringWriter;
import java.util.Properties;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

  @Autowired
  private JavaMailSender javaMailSender;

  private VelocityEngine velocityEngine;

  @PostConstruct
  public void init() {
    velocityEngine = new VelocityEngine();
    Properties p = new Properties();
    p.setProperty("resource.loader", "class");
    p.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
    p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    velocityEngine.init(p);
  }


  @Value("${smtp.from.address}")
  private String fromAddress;

  @Override
  public void sendResetPasswordLink(String emailAddress, String url) {
    VelocityContext context = new VelocityContext();
    context.put("summary", "Reset your MyDialBuddy account password.");
    context.put("header", "Reset Your Password");
    context.put("linkInstructions", "Please click on the link below to reset the password on your MyDialBuddy account.");
    context.put("link", url);
    context.put("linkHelp", "This link will remain valid for 24 hours.");
    StringWriter sw = new StringWriter();
    velocityEngine.mergeTemplate("/template/linkEmail.vm", "utf-8", context, sw);
    sendEmail(emailAddress, "Reset your MyDialBuddy password", sw.toString());
  }

  @Override
  public void sendRecordingLink(String emailAddress, String url) {

  }

  private void sendEmail(String emailAddress, String subject, String body) {
    MimeMessagePreparator messagePreparator = mimeMessage -> {
      MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
      messageHelper.setFrom(fromAddress);
      messageHelper.setTo(emailAddress);
      messageHelper.setSubject(subject);
      messageHelper.setText(body, true);
    };
    try {
      javaMailSender.send(messagePreparator);
      log.info("Sent email to {}", emailAddress);
    } catch( MailException ex) {
      log.error("Error occurred sending email -> {}", ex);
    }
  }

}
