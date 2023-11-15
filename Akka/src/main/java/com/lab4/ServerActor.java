package com.lab4;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

// Only Akka actor we have in this example
public class ServerActor extends AbstractActorWithStash {

    public ServerActor() {
    }

    // Specifies a single match clause that looks at messages
    @Override
    public Receive createReceive() {
        return createAwakeReceive();
    }

    private Receive createAwakeReceive() {
        return receiveBuilder() // Whenever a message of the SimpleMessage class is received, call onMessage
                .match(SleepMessage.class, this::onSleepMessage)
                .match(Message.class, this::onMessageStatusWake)
                .build();
    }

    private Receive createSleepingReceive() {
        return receiveBuilder() // Whenever a message of the SimpleMessage class is received, call onMessage
                .match(WakeupMessage.class, this::onWakeupMessage)
                .match(Message.class, this::onMessageStatusSleep)
                .build();
    }

    private void onSleepMessage(SleepMessage p) {
        getContext().become(createSleepingReceive());
        System.out.println("I sleep");
    }

    private void onWakeupMessage(WakeupMessage p) {
        getContext().become(createAwakeReceive());
        System.out.println("I wake up");
        unstashAll();
    }

    private void onMessageStatusWake(Message p) {
        sender().tell(p,self());
    }

    private void onMessageStatusSleep(Message p) {
        stash();
    }

    // Used by the props static class to create the actor
    static Props props() {
        return Props.create(ServerActor.class);
    }

}
