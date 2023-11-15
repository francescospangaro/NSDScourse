package com.lab5;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import com.faultTolerance.counter.CounterSupervisorActor;

import java.time.Duration;

public class SupervisorActor extends AbstractActor{
    private final static int retryNumber = 1;
    private final static int timeLength = 1;

    // #strategy
    // We are instantiating a strategy for a One-for-one pattern, it works by
    // declaring the maximum number of faults(1) within a given time period(2),
    // and saying what we need to do whenever we detect a fault(3)
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    retryNumber, // Max no of retries
                    Duration.ofMinutes(timeLength), // Within what time period
                    DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        // Creates the child actor within the supervisor actor context
        return receiveBuilder()
                .match(
                        Props.class,
                        props -> {
                            getSender().tell(getContext().actorOf(props), getSelf());
                        })
                .build();
    }

    static Props props() {
        return Props.create(SupervisorActor.class);
    }

}
