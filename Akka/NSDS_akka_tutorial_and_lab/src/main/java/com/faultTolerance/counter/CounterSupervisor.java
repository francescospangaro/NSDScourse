package com.faultTolerance.counter;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class CounterSupervisor {
    public static final int FAULT_OP = -1;
    // Different supervision strategies:
    //	1. One-for-one: Actions for fault tolerance are applied only to the faulty worker by the supervisor
    //	2. One-for-all: When the supervisor detects a fault, it applies fault tolerance actions to each actor
    //					under him, without regards of faulty or not
    public static final int NORMAL_OP = 0;

    public static final int FAULTS = 1;

    public static void main(String[] args) {
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        // Create actor system
        final ActorSystem sys = ActorSystem.create("System");
        // Create the supervisor actor, it MUST be created in its own local content
        final ActorRef supervisor = sys.actorOf(CounterSupervisorActor.props(), "supervisor");

        ActorRef counter;
        try {

            // Asks the supervisor to create the child actor and returns a reference
            // it creates the actor in the local content of the supervisor, we ask the supervisor to do so,
            // and return to us the new actor
            scala.concurrent.Future<Object> waitingForCounter = ask(supervisor, Props.create(CounterActor.class), 5000);
            // child actor created by the supervisor
            counter = (ActorRef) waitingForCounter.result(timeout, null);

            // Here we tell the child actor what to do
            // Sends a normal message
            counter.tell(new DataMessage(NORMAL_OP), ActorRef.noSender());

            // Sends a faulty message (here FAULTS is 1)
            for (int i = 0; i < FAULTS; i++)
                counter.tell(new DataMessage(FAULT_OP), ActorRef.noSender());


            counter.tell(new DataMessage(NORMAL_OP), ActorRef.noSender());

            sys.terminate();

        } catch (TimeoutException | InterruptedException e1) {

            e1.printStackTrace();
        }

    }

}
