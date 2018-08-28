package com.mishima.callrecorder.twiliocallhandler.controller;

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
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Say.Voice;
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
    Optional<Account> result = accountService.findByPhoneNumbers(from);
    if(!result.isPresent()) {
      log.info("No account found for incoming call from {}", from);
      response = new VoiceResponse.Builder().say(noAccount()).build();
    } else {
      // Store details of this call in the session
      Account account = result.get();
      request.getSession().setAttribute("CallSid", callSid);
      request.getSession().setAttribute("AccountId", account.getId());
      request.getSession().setAttribute("Trial", trial);

      // Publish call initiated publisher
      log.info("Publishing call initiated publisher.");
      eventPublisher.publish(eventTopicArn, Event.builder()
          .eventType(EventType.CallInitiated)
          .callSid(callSid)
          .attribute("AccountId", account.getId())
          .attribute("From", from)
          .attribute("Trial", trial)
          .build());
      // Generate response
      Gather gather = new Gather.Builder().action(baseUri + "/confirm").method(HttpMethod.POST)
          .timeout(20).say(instructions()).build();
      response = new VoiceResponse.Builder().gather(gather).say(noResponse()).build();
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
          .timeout(20).say(noDigits()).build();
      response = new VoiceResponse.Builder().gather(gather).say(noResponse()).build();
      return buildResponseEntity(response.toXml());
    } else {
      // Store captured number in session
      request.getSession().setAttribute("Number", digits);

      log.info("Confirming digits with caller");
      Gather gather = new Gather.Builder().action(baseUri + "/confirmed")
          .method(HttpMethod.POST).numDigits(1).timeout(20).say(confirm(digits)).build();
      response = new VoiceResponse.Builder().gather(gather).say(noResponse()).build();
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
          .timeout(20).say(noDigits()).build();
      response = new VoiceResponse.Builder().gather(gather).say(noResponse()).build();
    } else {
      if ("1".equals(digits)) {
        String number = (String)request.getSession().getAttribute("Number");
        log.info("Caller confirmed number {}, validating...", number);
        if(!dialledNumberValidator.checkNumberIsValid(number)) {
          log.info("Requested number to dial invalid, ask for digits again");
          Gather gather = new Gather.Builder().action(baseUri + "/confirm").method(HttpMethod.POST)
              .timeout(20).say(invalidNumber()).build();
          response = new VoiceResponse.Builder().gather(gather).say(noResponse()).build();
        } else {
          // Get trial option from session
          boolean trial = (Boolean)request.getSession().getAttribute("Trial");
          if( trial) {
            Say say = say("Connecting your trial call, this call will automatically disconnect after 2 minutes.");
            Dial dial = new Dial.Builder().callerId(from).record(Dial.Record.RECORD_FROM_ANSWER).timeout(30).timeLimit(120)
                .recordingStatusCallback(baseUri + "/recording").recordingStatusCallbackMethod(HttpMethod.POST)
                .number(new Number.Builder(number).build()).build();
            response = new VoiceResponse.Builder().say(say).dial(dial).build();
          } else {
            Say say = say("Connecting your call.");
            Dial dial = new Dial.Builder().callerId(from).record(Dial.Record.RECORD_FROM_ANSWER).timeout(30)
                .recordingStatusCallback(baseUri + "/recording").recordingStatusCallbackMethod(HttpMethod.POST)
                .number(new Number.Builder(number).build()).build();
            response = new VoiceResponse.Builder().say(say).dial(dial).build();
          }
          return buildResponseEntity(response.toXml());
        }
      } else {
        log.info("Caller did not confirm number, ask for digits again");
        Gather gather = new Gather.Builder().action(baseUri + "/confirm").method(HttpMethod.POST)
            .timeout(20).say(instructions()).build();
        response = new VoiceResponse.Builder().gather(gather).say(noResponse()).build();
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

  private ResponseEntity<byte[]> buildResponseEntity(String xml) {
    log.info("Sending response xml: {}", xml);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_XML);
    headers.setCacheControl("no-cache");
    return new ResponseEntity<>(xml.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
  }


  private Say instructions() {
    return say("Please enter the number you wish to call on your keypad followed by the pound or hash key.");
  }

  private Say confirm(String digits) {
    StringBuilder confirmDigits = new StringBuilder();
    String delim = "";
    for (char c : digits.toCharArray()) {
      confirmDigits.append(delim).append(c);
      delim = ",";
    }
    return say("I heard " + confirmDigits
        + ". Is that correct? Press 1 to confirm or any other key to try again.");
  }

  private Say noDigits() {
    return say("Sorry, I didn't get that. Please enter the number you wish to call on your keypad followed by the pound or hash key.");
  }

  private Say noResponse() {
    return say("Sorry I didn't get any input, Goodbye!");
  }

  private Say noAccount() {
    return say("Please visit our website to register an account with this phone number!");
  }

  private Say invalidNumber() {
    return say("I'm sorry, that appears to be an invalid number. Please note that only calls to domestic and non-premium numbers are supported. Please enter the number you wish to call on your keypad followed by the pound or hash key.");
  }

  private Say say(String message) {
    return new Say.Builder(message).voice(Voice.ALICE).build();
  }

}
