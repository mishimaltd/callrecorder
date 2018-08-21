package com.mishima.callrecorder.actor;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public interface ActorFactory {

  ActorRef create(ActorContext context);

}
