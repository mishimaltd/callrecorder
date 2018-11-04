package com.mishima.callrecorder.twiliocallhandler.controller;

import com.google.common.collect.ImmutableMap;
import com.mishima.callrecorder.accountservice.entity.Account;
import com.mishima.callrecorder.accountservice.service.AccountService;
import com.mishima.callrecorder.publisher.Publisher;
import com.mishima.callrecorder.publisher.entity.Event;
import com.mishima.callrecorder.publisher.entity.Event.EventType;
import com.mishima.callrecorder.twiliocallhandler.validator.DialledNumberValidator;
import com.twilio.http.HttpMethod;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Number;
import com.twilio.twiml.voice.Play;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/twilio")
public class TwilioRestController {

  private static final String basePlayUri = "https://s3.amazonaws.com/callrecorder-static/voice/";

  private static final ImmutableMap<Character, String> digitsToFileMap = ImmutableMap.<Character, java.lang.String>builder()
      .put('0', "zero.mp3")
      .put('1', "one.mp3")
      .put('2', "two.mp3")
      .put('3', "three.mp3")
      .put('4', "four.mp3")
      .put('5', "five.mp3")
      .put('6', "six.mp3")
      .put('7', "seven.mp3")
      .put('8', "eight.mp3")
      .put('9', "nine.mp3")
      .build();

  @Autowired
  private AccountService accountService;

  @Autowired
  private Publisher eventPublisher;

  @Autowired
  private DialledNumberValidator dialledNumberValidator;

  @Value("${twilio.baseUri}")
  private String baseUri;

  @Value("${event.topic.arn}")
  private String eventTopicArn;

  @ResponseBody
  @PostMapping(value = "/receive", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<byte[]> receive(@RequestParam("CallSid") String callSid,
                                        @RequestParam("From") String from,
                                        @RequestParam(value = "Trial", required = false, defaultValue = "false") boolean trial,
                                        HttpServletRequest request) throws TwiMLException {
    log.info("Received call sid {} from number {}", callSid, from);
    VoiceResponse response;
    // Check there is an account associated with the inbound number
    Optional<String> accountId = getAccountId(trial, from);
    if(!accountId.isPresent()) {
      log.info("No account found for incoming call from {}", from);
      response = new VoiceResponse.Builder().play(noAccount()).build();
    } else {
      // Store details of this call in the session
      request.getSession().setAttribute("Trial", trial);
      request.getSession().setAttribute("CallSid", callSid);
      request.getSession().setAttribute("AccountId", accountId.get());

      // Publish call initiated publisher
      log.info("Publishing call initiated publisher.");
      eventPublisher.publish(eventTopicArn, Event.builder()
          .eventType(EventType.CallInitiated)
          .callSid(callSid)
          .attribute("AccountId", accountId.get())
          .attribute("From", from)
          .attribute("Trial", trial)
          .build());
      // Generate response
      Gather gather = new Gather.Builder().action(baseUri + "/confirm").method(HttpMethod.POST)
          .timeout(20).play(instructions(trial)).build();
      response = new VoiceResponse.Builder().gather(gather).play(noResponse()).build();
    }
    return buildResponseEntity(response.toXml());
  }

  @ResponseBody
  @PostMapping(value = "/completed", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<byte[]> completed(@RequestParam("CallSid") String callSid,
                                          @RequestParam("CallDuration") int callDuration) {
    log.info("Received call completed for call sid {}, duration {}", callSid, callDuration);
    // Publish call ended publisher
    log.info("Publishing call completed publisher.");
    eventPublisher.publish(eventTopicArn, Event.builder()
      .eventType(EventType.CallEnded)
      .callSid(callSid)
      .attribute("CallDuration", callDuration)
      .build());
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @ResponseBody
  @PostMapping(value = "/confirm", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<byte[]> confirm(@RequestParam("CallSid") String callSid,
                                        @RequestParam("From") String from,
                                        @RequestParam(value = "Digits", defaultValue = "") String digits,
                                        HttpServletRequest request) throws TwiMLException {
    log.info("Received call sid {} from number {} with captured digits {}", callSid, from, digits);
    VoiceResponse response;
    if (!StringUtils.hasText(digits)) {
      log.info("Did not capture any digits from the call");
      Gather gather = new Gather.Builder().action(baseUri + "/confirm").method(HttpMethod.POST)
          .timeout(20).play(noDigits()).build();
      response = new VoiceResponse.Builder().gather(gather).play(noResponse()).build();
      return buildResponseEntity(response.toXml());
    } else {
      // Store captured number in session
      request.getSession().setAttribute("Number", digits);

      log.info("Confirming digits with caller");
      Gather.Builder builder = new Gather.Builder().action(baseUri + "/confirmed")
          .method(HttpMethod.POST).numDigits(1).timeout(20);
      confirm(builder, digits);
      Gather gather = builder.build();

      response = new VoiceResponse.Builder().gather(gather).play(noResponse()).build();
    }
    return buildResponseEntity(response.toXml());
  }

  @ResponseBody
  @PostMapping(value = "/confirmed", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<byte[]> confirmed(@RequestParam("CallSid") String callSid,
                                          @RequestParam("From") String from,
                                          @RequestParam(value = "Digits", defaultValue = "") String digits,
                                          HttpServletRequest request) throws TwiMLException {
    log.info("Received call sid {} from number {} with captured digits {}", callSid, from, digits);
    VoiceResponse response;
    if (!StringUtils.hasText(digits)) {
      log.info("Did not capture any digits from the call");
      Gather gather = new Gather.Builder().action(baseUri + "/confirm").method(HttpMethod.POST)
          .timeout(20).play(noDigits()).build();
      response = new VoiceResponse.Builder().gather(gather).play(noResponse()).build();
    } else {
      // Get trial option from session
      boolean trial = (Boolean)request.getSession().getAttribute("Trial");
      if ("1".equals(digits)) {
        String number = (String)request.getSession().getAttribute("Number");
        log.info("Caller confirmed number {}, validating...", number);
        if(!dialledNumberValidator.checkNumberIsValid(number)) {
          log.info("Requested number to dial invalid, ask for digits again");
          Gather gather = new Gather.Builder().action(baseUri + "/confirm").method(HttpMethod.POST)
              .timeout(20).play(invalidNumber()).build();
          response = new VoiceResponse.Builder().gather(gather).play(noResponse()).build();
        } else {
          if( trial) {
            Dial dial = new Dial.Builder().callerId(from).record(Dial.Record.RECORD_FROM_ANSWER).timeout(30).timeLimit(300)
                .recordingStatusCallback(baseUri + "/recording").recordingStatusCallbackMethod(HttpMethod.POST)
                .number(new Number.Builder(number).build()).build();
            response = new VoiceResponse.Builder().play(play("trial_connect.mp3")).dial(dial).build();
          } else {
            Dial dial = new Dial.Builder().callerId(from).record(Dial.Record.RECORD_FROM_ANSWER).timeout(30)
                .recordingStatusCallback(baseUri + "/recording").recordingStatusCallbackMethod(HttpMethod.POST)
                .number(new Number.Builder(number).build()).build();
            response = new VoiceResponse.Builder().play(play("connect.mp3")).dial(dial).build();

            // Send call number confirmed event
            eventPublisher.publish(eventTopicArn, Event.builder()
                .eventType(EventType.NumberConfirmed)
                .callSid(callSid)
                .attribute("Number", number)
                .build());
          }
          return buildResponseEntity(response.toXml());
        }
      } else {
        log.info("Caller did not confirm number, ask for digits again");
        Gather gather = new Gather.Builder().action(baseUri + "/confirm").method(HttpMethod.POST)
            .timeout(20).play(instructions(trial)).build();
        response = new VoiceResponse.Builder().gather(gather).play(noResponse()).build();
      }
    }
    return buildResponseEntity(response.toXml());
  }

  @ResponseBody
  @PostMapping(value = "/recording", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<byte[]> recording(
                       @RequestParam("CallSid") String callSid,
                       @RequestParam("RecordingSid") String recordingSid,
                       @RequestParam("RecordingUrl") String recordingUrl,
                       @RequestParam("RecordingDuration") int recordingDuration,
                       @RequestParam("RecordingStatus") String recordingStatus) {
    log.info("Received call sid {}, status {}, recording sid {}, recording duration {}", callSid, recordingStatus, recordingSid, recordingDuration);
    if("completed".equals(recordingStatus)) {
      log.info("Publishing recording completed publisher for call sid {}", callSid);
      eventPublisher.publish(eventTopicArn, Event.builder()
          .eventType(EventType.CallRecordingCompleted)
          .callSid(callSid)
          .attribute("RecordingSid", recordingSid)
          .attribute("RecordingUrl", recordingUrl)
          .attribute("RecordingDuration", recordingDuration)
          .build());
    } else {
      log.warn("Error occurred publishing recording, sending error event");
      eventPublisher.publish(eventTopicArn, Event.builder()
          .eventType(EventType.Error)
          .callSid(callSid)
          .attribute("Message", "Received recording status " + recordingStatus)
          .build());
    }
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  private Optional<String> getAccountId(boolean trial, String from) {
    if(trial) return Optional.of("trial");
    Optional<Account> result = accountService.findByPhoneNumbers(from);
    return result.map(Account::getId);
  }

  private ResponseEntity<byte[]> buildResponseEntity(String xml) {
    log.info("Sending response xml: {}", xml);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_XML);
    headers.setCacheControl("no-cache");
    return new ResponseEntity<>(xml.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
  }


  private Play instructions(boolean trial) {
    return trial? play("trial.mp3"): play("welcome.mp3");
  }

  private void confirm(Gather.Builder builder, String digits) {
    builder.play(play("i_heard.mp3"));
    for (char c : digits.toCharArray()) {
      if(digitsToFileMap.containsKey(c)) {
        builder.play(play(digitsToFileMap.get(c)));
      }
    }
    builder.play(play("is_that_right.mp3"));
  }

  private Play noDigits() {
    return play("im_sorry.mp3");
  }

  private Play noResponse() {
    return play("no_input.mp3");
  }

  private Play noAccount() {
    return play("no_account.mp3");
  }

  private Play invalidNumber() {
    return play("invalid_number.mp3");
  }

  private Play play(String file) {
    return new Play.Builder().url(basePlayUri + file).build();
  }

}
