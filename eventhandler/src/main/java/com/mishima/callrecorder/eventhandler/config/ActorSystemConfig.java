package com.mishima.callrecorder.eventhandler.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.mishima.callrecorder.actor.SupervisorActor;
import com.mishima.callrecorder.eventhandler.actor.factory.EventActorFactory;
import com.mishima.callrecorder.eventhandler.handler.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActorSystemConfig {

  @Autowired
  private EventActorFactory actorFactory;

  @Bean
  public EventHandler eventHandler() {
    ActorSystem actorSystem = ActorSystem.create();
    ActorRef supervisor = actorSystem.actorOf(Props.create(SupervisorActor.class, actorFactory));
    return new EventHandler(supervisor);
  }

}
