package com.lab4;

import akka.actor.ActorRef;

public record ServerReferenceMessage(ActorRef server) {
}
