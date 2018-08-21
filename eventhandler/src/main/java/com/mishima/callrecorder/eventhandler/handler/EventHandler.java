package com.mishima.callrecorder.eventhandler.handler;

import akka.actor.ActorRef;
import com.mishima.callrecorder.publisher.entity.Event;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventHandler {

  private final ActorRef supervisor;

  public EventHandler(ActorRef supervisor) {
    this.supervisor = supervisor;
  }

  public void handle(Event event) {
    supervisor.tell(event, ActorRef.noSender());
  }

}
