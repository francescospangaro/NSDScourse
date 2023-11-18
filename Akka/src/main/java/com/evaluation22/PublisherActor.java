package com.evaluation22;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class PublisherActor extends AbstractActor {
    private ActorRef broker;

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMsg.class, this::configure)
                .match(PublishMsg.class, this::onPublish).build();
    }

    private void configure(ConfigMsg msg) {
        System.out.println("PUBLISHER: Received configuration message!");
        broker = msg.getBrokerRef();
    }

    private void onPublish(PublishMsg msg) {
        System.out.println("PUBLISHER: Received publish command!");
        broker.tell(msg, self());
    }

    static Props props() {
        return Props.create(PublisherActor.class);
    }
}
