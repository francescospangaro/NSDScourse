package com.lab5;

import akka.actor.ActorRef;

public record ServerReferenceMessage(ActorRef server) {
}
