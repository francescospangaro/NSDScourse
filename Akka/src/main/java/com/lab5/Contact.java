package com.lab5;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Contact {

    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) throws InterruptedException, TimeoutException {
        ActorSystem sys = ActorSystem.create("Server");


        // Creates the actorSystem itself, it's where all the actors will work
        // Create one single counterActor using the props method in the CounterActor class, and actorOf to instantiate
        // this new actor in the ActorSystem declared above
        final ActorRef supervisor = sys.actorOf(SupervisorActor.props(), "supervisor");

        ActorRef server;

        //Faccio creare al supervisor il contactActor
        scala.concurrent.Future<Object> waitingForCounter = ask(supervisor, Props.create(ContactActor.class), 5000);
        server = (ActorRef) waitingForCounter.result(Duration.create(5, SECONDS), null);

        //Creo il client impostandogli il server
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");
        client.tell(new ServerReferenceMessage(server), ActorRef.noSender());

        client.tell(new PutMessage("sacca", "viso@a.cloud"), ActorRef.noSender());
        client.tell(new PutMessage("spanga", "paolo@b.cloud"), ActorRef.noSender());
        client.tell(new GetMessage("sacca"), ActorRef.noSender());
        client.tell(new GetMessage("spanga"), ActorRef.noSender());

        client.tell(new PutMessage("Fail", "viso@a.cloud"), ActorRef.noSender());

        client.tell(new GetMessage("sacca"), ActorRef.noSender());
        client.tell(new GetMessage("spanga"), ActorRef.noSender());

        // Wait for all messages to be sent and received
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sys.terminate();
    }

}
