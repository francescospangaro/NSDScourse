package com.lab2;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

// Only Akka actor we have in this example
public class CounterActor extends AbstractActorWithStash {

    private int counter;

    public CounterActor() {
        this.counter = 0;
    }

    // Specifies a single match clause that looks at messages
    @Override
    public Receive createReceive() {
        return receiveBuilder() // Whenever a message of the SimpleMessage class is received, call onMessage
                .match(DecrementMessage.class, this::onIncrementMessage)
                .match(IncrementMessage.class, this::onDecrementMessage)
                .build();
    }


    void onDecrementMessage(IncrementMessage msg) {
        if (counter == 0) {
            stash();
            System.out.println("Stashed");
        } else {
            --counter;
            System.out.println("Counter decremented to " + counter);
        }
    }

    // Takes the simpleMessage received, increments the counter and prints it in the console
    void onIncrementMessage(DecrementMessage msg) {
        ++counter;
        unstash();
        System.out.println("Counter increased to " + counter);
    }

    // Used by the props static class to create the actor
    static Props props() {
        return Props.create(CounterActor.class);
    }

}
