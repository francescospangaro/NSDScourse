package com.lab3;

import akka.actor.ActorRef;

public record ServerReferenceMessage(ActorRef server) {
}
