package com.lab4;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import static akka.pattern.Patterns.ask;

public class ClientActor extends AbstractActor {
    ActorRef server;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ServerReferenceMessage.class,this::setServer)
                .match(Message.class, this::onMessage)
                .match(SleepMessage.class, this::onSleepMessage)
                .match(WakeupMessage.class, this::onWakeupMessage)
                .build();
    }

    private void onMessage(Message msg) {
        if(sender().equals(server)){
            System.out.println("Client received: "+msg);
        }else{
            //E' il main che mi ha inviato questo messaggio, quindi I tell the server
            server.tell(msg, self());
        }

    }

    private void onSleepMessage(SleepMessage msg) {
        server.tell(msg, self());
    }

    private void onWakeupMessage(WakeupMessage msg) {
        server.tell(msg, self());
    }

    private void setServer(ServerReferenceMessage msg) {
        server = msg.server();
    }

    static Props props() {
        return Props.create(ClientActor.class);
    }

}
