package com.mishima.callrecorder.commandhandler.handler;

import akka.actor.ActorRef;
import com.mishima.callrecorder.publisher.entity.Command;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandHandler {

  private final ActorRef supervisor;

  public CommandHandler(ActorRef supervisor) {
    this.supervisor = supervisor;
  }

  public void handle(Command command) {
    supervisor.tell(command, ActorRef.noSender());
  }

}
