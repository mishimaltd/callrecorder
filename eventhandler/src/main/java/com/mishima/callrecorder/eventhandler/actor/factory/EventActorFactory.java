package com.mishima.callrecorder.eventhandler.actor.factory;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.mishima.callrecorder.actor.ActorFactory;
import com.mishima.callrecorder.callservice.service.CallService;
import com.mishima.callrecorder.eventhandler.actor.EventActor;
import com.mishima.callrecorder.publisher.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventActorFactory implements ActorFactory {

  @Autowired
  private CallService callService;

  @Autowired
  private Publisher publisher;

  @Value("${command.topic.arn}")
  private String commandTopicArn;

  @Override
  public ActorRef create(ActorContext context) {
    return context.actorOf(Props.create(EventActor.class, callService, publisher, commandTopicArn));
  }
}
