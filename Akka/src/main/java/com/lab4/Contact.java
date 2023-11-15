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

    public static void main(String[] args) {
        Config conf = ConfigFactory.parseFile(new File("conf"));
        ActorSystem sys = ActorSystem.create("Server", conf);


        // Creates the actorSystem itself, it's where all the actors will work
        // Create one single counterActor using the props method in the CounterActor class, and actorOf to instantiate
        // this new actor in the ActorSystem declared above
        final ActorRef contact = sys.actorOf(ServerActor.props(), "contact");

        final ActorRef client = sys.actorOf(ClientActor.props(), "client");
        client.tell(new WakeupMessage(contact), ActorRef.noSender());
        client.tell(new SleepMessage("sacca", "chiappetta@saccani.cloud"), ActorRef.noSender());
        client.tell(new SleepMessage("spanga", "sederino@saccani.cloud"), ActorRef.noSender());
        client.tell(new Message("spanga"), ActorRef.noSender());
        client.tell(new Message("sacca"), ActorRef.noSender());


        // Wait for all messages to be sent and received
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sys.terminate();
    }

}
