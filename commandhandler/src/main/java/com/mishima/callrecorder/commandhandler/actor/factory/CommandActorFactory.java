package com.mishima.callrecorder.commandhandler.actor.factory;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.mishima.callrecorder.actor.ActorFactory;
import com.mishima.callrecorder.callservice.service.CallService;
import com.mishima.callrecorder.commandhandler.actor.CommandActor;
import com.mishima.callrecorder.commandhandler.tinyurl.TinyUrlService;
import com.mishima.callrecorder.publisher.Publisher;
import com.mishima.callrecorder.s3service.service.S3Service;
import com.mishima.callrecorder.twilioservice.TwilioRecordingDeleterService;
import com.mishima.callrecorder.twilioservice.TwilioSMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommandActorFactory implements ActorFactory {

  @Autowired
  private Publisher publisher;

  @Value("${event.topic.arn}")
  private String eventTopicArn;

  @Autowired
  private CallService callService;

  @Autowired
  private S3Service s3Service;

  @Autowired
  private TwilioSMSService twilioSMSService;

  @Autowired
  private TwilioRecordingDeleterService twilioRecordingDeleterService;

  @Autowired
  private TinyUrlService tinyUrlService;

  @Override
  public ActorRef create(ActorContext context) {
    return context.actorOf(Props.create(
        CommandActor.class,
        publisher,
        callService,
        eventTopicArn,
        s3Service,
        twilioSMSService,
        twilioRecordingDeleterService,
        tinyUrlService
    ));
  }

}
