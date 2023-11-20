package com.counter;

import akka.actor.AbstractActor;
import akka.actor.Props;

// Only Akka actor we have in this example
public class CounterActor extends AbstractActor {

    private int counter;

    public CounterActor() {
        this.counter = 0;
    }

    // Specifies a single match clause that looks at messages
    @Override
    public Receive createReceive() {
        return receiveBuilder() // Whenever a message of the SimpleMessage class is received, call onMessage
                .match(SimpleMessage.class, this::onMessage)
                .match(OtherMessage.class, this::onOtherMessage)
                .build();
    }

    void onOtherMessage(OtherMessage msg) {
        System.out.println("OtherMessage type received");
    }

    // Takes the simpleMessage received, increments the counter and prints it in the console
    void onMessage(SimpleMessage msg) {
        ++counter;
        System.out.println("Counter increased to " + counter);
    }

    // Used by the props static class to create the actor
    static Props props() {
        return Props.create(CounterActor.class);
    }

}
