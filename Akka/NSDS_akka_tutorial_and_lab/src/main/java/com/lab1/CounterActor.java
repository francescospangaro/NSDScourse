package com.lab1;

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
                .match(DecrementMessage.class, this::onIncrementMessage)
                .match(IncrementMessage.class, this::onDecrementMessage)
                .match(Message.class, this::onMessage)
                .build();
    }

    void onMessage(Message msg) {
        if (msg.increment()) {
            counter++;
            System.out.println("Counter incremented to " + counter);
        } else {
            counter--;
            System.out.println("Counter decremented to " + counter);
        }
    }

    void onDecrementMessage(IncrementMessage msg) {
        --counter;
        System.out.println("Counter decremented to " + counter);
    }

    // Takes the simpleMessage received, increments the counter and prints it in the console
    void onIncrementMessage(DecrementMessage msg) {
        ++counter;
        System.out.println("Counter increased to " + counter);
    }

    // Used by the props static class to create the actor
    static Props props() {
        return Props.create(CounterActor.class);
    }

}
