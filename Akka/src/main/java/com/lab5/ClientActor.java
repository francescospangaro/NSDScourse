package com.lab5;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;

public class ClientActor extends AbstractActor {
    ActorRef server;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetMessage.class, this::onGetMessage)
                .match(PutMessage.class, this::onPutMessage)
                .match(ServerReferenceMessage.class, this::setServer)
                .build();
    }

    private void onGetMessage(GetMessage msg) throws InterruptedException, TimeoutException {
        String res = (String) ask(server, msg, 500)
                .result(Duration.create(5, TimeUnit.SECONDS), null);
        System.out.println("[Client]: " + res);
    }

    private void onPutMessage(PutMessage msg) {
        server.tell(msg, self());
    }

    private void setServer(ServerReferenceMessage msg) {
        server = msg.server();
    }

    static Props props() {
        return Props.create(ClientActor.class);
    }

}
