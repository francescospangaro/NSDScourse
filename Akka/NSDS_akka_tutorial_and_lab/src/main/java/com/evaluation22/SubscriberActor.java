package com.evaluation22;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class SubscriberActor extends AbstractActor {
    private ActorRef broker;

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMsg.class, this::configure)
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(NotifyMsg.class, this::onNotify).build();
    }

    private void configure(ConfigMsg msg) {
        System.out.println("SUBSCRIBER: Received configuration message!");
        broker = msg.getBrokerRef();
    }

    private void onSubscribe(SubscribeMsg msg) {
        System.out.println("SUBSCRIBER: Received subscribe command!");
        broker.tell(msg, self());
    }

    private void onNotify(NotifyMsg msg) {
        System.out.println("SUBSCRIBER: Received notify message!");
        System.out.println("Received value: " + msg.getValue());
    }

    static Props props() {
        return Props.create(SubscriberActor.class);
    }
}
