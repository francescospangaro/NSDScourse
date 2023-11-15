package com.lab4;

import akka.actor.AbstractActor;
import akka.actor.Props;

// Only Akka actor we have in this example
public class ServerActor extends AbstractActor {

    public ServerActor() {
    }

    // Specifies a single match clause that looks at messages
    @Override
    public Receive createReceive() {
        return createWakeupReceive();
    }

    private Receive createAwakeReceive() {
        return receiveBuilder() // Whenever a message of the SimpleMessage class is received, call onMessage
                .match(SleepMessage.class, this::onSleepMessage)
                .match(Message.class, this::onMessage)
                .build();
    }

    private Receive createSleepingReceive() {
        return receiveBuilder() // Whenever a message of the SimpleMessage class is received, call onMessage
                .match(WakeupMessage.class, this::on)
                .match(Message.class, this::onMessage)
                .build();
    }

    private void onSleepMessage(SleepMessage p) {
        getContext().become(createSleepingReceive());
        System.out.println("Mi addormento");
    }




    private void onSleepMessage(SleepMessage p) {
        getContext().become(createSleepingReceive());
        System.out.println("Mi addormento");
    }

    private void onMessage(Message p) {
        String email = contacts.get(p.name());
        getSender().tell(email, self());
        System.out.println("Sent " + email);
    }

    // Used by the props static class to create the actor
    static Props props() {
        return Props.create(ServerActor.class);
    }

}
