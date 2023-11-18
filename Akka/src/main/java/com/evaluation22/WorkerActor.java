package com.evaluation22;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Map;

public class WorkerActor extends AbstractActor {
    private final Map<String, ActorRef> subscriptions = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(PublishMsg.class, this::onPublish)
                .build();
    }

    private void onSubscribe(SubscribeMsg msg) {
        subscriptions.put(msg.getTopic(), msg.getSender());
    }

    private void onPublish(PublishMsg msg) throws Exception {
        if (!subscriptions.containsKey(msg.getTopic())) {
            throw new Exception("Topic not registered " + getContext().getSelf().toString());
        }
        subscriptions.get(msg.getTopic()).tell(new NotifyMsg(msg.getValue()), self());
    }

    static Props props() {
        return Props.create(WorkerActor.class);
    }
}
