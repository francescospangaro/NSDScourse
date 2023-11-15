package com.lab2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Counter {

    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) {
        // Creates the actorSystem itself, it's where all the actors will work
        final ActorSystem sys = ActorSystem.create("System");
        // Create one single counterActor using the props method in the CounterActor class, and actorOf to instantiate
        // this new actor in the ActorSystem declared above
        final ActorRef counter = sys.actorOf(CounterActor.props(), "counter");

        // Send messages from multiple threads in parallel
        // Just java stuff
        final ExecutorService exec = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numMessages; i++) {
            // sends a message to the CounterActor, this is done in parallel
            // empty single messages sent by noSender, means that they come from an
            // anonymous server, we don't specify the sender
            exec.submit(() -> counter.tell(new DecrementMessage(), ActorRef.noSender()));
            exec.submit(() -> counter.tell(new IncrementMessage(), ActorRef.noSender()));
        }

        // Wait for all messages to be sent and received
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        exec.shutdown();
        sys.terminate();

    }

}
