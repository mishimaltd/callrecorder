package com.mishima.callrecorder.commandhandler.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.mishima.callrecorder.actor.SupervisorActor;
import com.mishima.callrecorder.commandhandler.actor.factory.CommandActorFactory;
import com.mishima.callrecorder.commandhandler.handler.CommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActorSystemConfig {

  @Autowired
  private CommandActorFactory actorFactory;

  @Bean
  public CommandHandler commandHandler() {
    ActorSystem actorSystem = ActorSystem.create();
    ActorRef supervisor = actorSystem.actorOf(Props.create(SupervisorActor.class, actorFactory));
    return new CommandHandler(supervisor);
  }

}
