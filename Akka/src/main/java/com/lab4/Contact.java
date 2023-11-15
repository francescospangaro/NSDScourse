package com.lab4;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.io.IOException;

public class Contact {

    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) throws InterruptedException {
        ActorSystem sys = ActorSystem.create("Server");


        // Creates the actorSystem itself, it's where all the actors will work
        // Create one single counterActor using the props method in the CounterActor class, and actorOf to instantiate
        // this new actor in the ActorSystem declared above
        final ActorRef server = sys.actorOf(ServerActor.props(), "server");

        final ActorRef client = sys.actorOf(ClientActor.props(), "client");
        client.tell(new ServerReferenceMessage(server), ActorRef.noSender());
        client.tell(new Message("1"), ActorRef.noSender());
        client.tell(new Message("2"), ActorRef.noSender());
        client.tell(new SleepMessage(), ActorRef.noSender());

        Thread.sleep(1000);

        client.tell(new Message("3"), ActorRef.noSender());
        client.tell(new Message("4"), ActorRef.noSender());
        client.tell(new WakeupMessage(), ActorRef.noSender());

        Thread.sleep(1000);

        client.tell(new Message("5"), ActorRef.noSender());
        client.tell(new Message("6"), ActorRef.noSender());

        // Wait for all messages to be sent and received
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sys.terminate();
    }

}
