package com.lab4;

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
                .match(Message.class, this::onGetMessage)
                .match(SleepMessage.class, this::onPutMessage)
                .match(WakeupMessage.class, this::setServer)
                .build();
    }

    private void onGetMessage(Message msg) throws InterruptedException, TimeoutException {
        String res = (String) ask(server, msg, 500)
                .result(Duration.create(5, TimeUnit.SECONDS), null);
        System.out.println("[Client]: " + res);
    }

    private void onPutMessage(SleepMessage msg) {
        server.tell(msg, self());
    }

    private void setServer(WakeupMessage msg) {
        server = msg.server();
    }

    static Props props() {
        return Props.create(ClientActor.class);
    }

}
