package com.mishima.callrecorder.app.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.mishima.callrecorder.actor.SupervisorActor;
import com.mishima.callrecorder.commandhandler.actor.factory.CommandActorFactory;
import com.mishima.callrecorder.commandhandler.handler.CommandHandler;
import com.mishima.callrecorder.eventhandler.actor.factory.EventActorFactory;
import com.mishima.callrecorder.eventhandler.handler.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActorSystemConfig {

  @Autowired
  private CommandActorFactory commandActorFactory;

  @Autowired
  private EventActorFactory eventActorFactory;

  @Bean
  public CommandHandler commandHandler() {
    ActorSystem actorSystem = ActorSystem.create();
    ActorRef supervisor = actorSystem.actorOf(Props.create(SupervisorActor.class, commandActorFactory));
    return new CommandHandler(supervisor);
  }

  @Bean
  public EventHandler eventHandler() {
    ActorSystem actorSystem = ActorSystem.create();
    ActorRef supervisor = actorSystem.actorOf(Props.create(SupervisorActor.class, eventActorFactory));
    return new EventHandler(supervisor);
  }

}
