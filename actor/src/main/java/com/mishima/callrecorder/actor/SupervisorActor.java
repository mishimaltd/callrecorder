package com.mishima.callrecorder.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.mishima.callrecorder.domain.entity.BaseMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupervisorActor extends AbstractActor {

  private final ActorFactory factory;

  private final Map<String, ActorRef> children = new HashMap<>();

  public SupervisorActor(ActorFactory factory) {
    this.factory = factory;
  }

  public Receive createReceive() {
    return receiveBuilder()
        .match(BaseMessage.class, this::handle)
        .build();
  }

  private void handle(BaseMessage message) {
    ActorRef child = children.computeIfAbsent(message.getCallSid(), s -> factory.create(context()));
    child.tell(message, self());
  }

}
