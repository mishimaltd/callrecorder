package com.mishima.callrecorder.emailservice;

import static java.time.ZoneOffset.UTC;

import com.mishima.callrecorder.callservice.entity.Call;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
    context.put("link", url);
    StringWriter sw = new StringWriter();
    velocityEngine.mergeTemplate("/template/forgotPasswordEmail.vm", "utf-8", context, sw);
    sendEmail(emailAddress, "Reset your MyDialBuddy password", sw.toString());
  }

  @Override
  public void sendRecordingLink(String emailAddress, Call call, String url) {
    Instant instant = Instant.ofEpochMilli (call.getCreated());
    ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, UTC);
    VelocityContext context = new VelocityContext();
    context.put("number", call.getTo());
    context.put("callDate", zdt.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
    context.put("link", url);
    context.put("duration", (int)Math.ceil(call.getRecordingDuration() / 60.0));
    context.put("cost",  NumberFormat.getCurrencyInstance(Locale.US).format(call.getCostInCents() / 100.0));
    context.put("linkHelp", "All your call recordings are stored securely in the cloud. The above link will remain valid for 7 days. Your If you want to access this recording after 7 days please log onto your dashboard at <a href=\"https://www.mydialbuddy.com/private/dashboard\">https://www.mydialbuddy.com/private/dashboard</a>.");
    StringWriter sw = new StringWriter();
    velocityEngine.mergeTemplate("/template/callRecordingEmail.vm", "utf-8", context, sw);
    sendEmail(emailAddress, "Your MyDialBuddy call recording", sw.toString());
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
