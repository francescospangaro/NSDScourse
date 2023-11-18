package com.evaluation22;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;

/**
 * È il supervisor degli workerActor
 */
public class BrokerActor extends AbstractActorWithStash {
    private final static int retryNumber = 1;
    private final static int timeLength = 1;

    private final ActorRef oddWorker, evenWorker;

    private static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    retryNumber, // Max no of retries
                    Duration.ofMinutes(timeLength), // Within what time period
                    DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                            .build());

    public BrokerActor() {
        oddWorker = getContext().actorOf(WorkerActor.props());
        evenWorker = getContext().actorOf(WorkerActor.props());
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }


    @Override
    public AbstractActor.Receive createReceive() {
        return batchedOff();
    }

    private Receive batchedOff() {
        return receiveBuilder()
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(PublishMsg.class, this::onPublish)
                .match(BatchMsg.class, this::onBatching).build();
    }

    private Receive batchedOn() {
        return receiveBuilder()
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(PublishMsg.class, this::onPubBatch)
                .match(BatchMsg.class, this::onBatching).build();
    }

    private void onSubscribe(SubscribeMsg msg) {
        if (msg.getKey() % 2 == 1) {
            System.out.println("BROKER: Subscribed to odd worker");
            oddWorker.tell(msg, self());
        } else {
            System.out.println("BROKER: Subscribed to even worker");
            evenWorker.tell(msg, self());
        }
    }

    private void onPublish(PublishMsg msg) {
        oddWorker.tell(msg, self());
        evenWorker.tell(msg, self());
    }

    private void onBatching(BatchMsg msg) {
        if (msg.isOn()) {
            System.out.println("BROKER: batching turned on");
            getContext().become(batchedOn());
        } else {
            System.out.println("BROKER: batching turned off");
            getContext().become(batchedOff());
            unstashAll();
        }
    }

    private void onPubBatch(PublishMsg msg) {
        System.out.println("BROKER: publish message stashed");
        stash();
    }

    static Props props() {
        return Props.create(BrokerActor.class);
    }
}
