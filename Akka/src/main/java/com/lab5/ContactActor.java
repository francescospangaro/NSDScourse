package com.lab5;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Map;

// Only Akka actor we have in this example
public class ContactActor extends AbstractActor {

    private final Map<String, String> contacts;

    public ContactActor() {
        this.contacts = new HashMap<>();
    }

    // Specifies a single match clause that looks at messages
    @Override
    public Receive createReceive() {
        return receiveBuilder() // Whenever a message of the SimpleMessage class is received, call onMessage
                .match(PutMessage.class, this::onPutMessage)
                .match(GetMessage.class, this::onGetMessage)
                .build();
    }

    private void onPutMessage(PutMessage p) throws Exception {
        if(p.name().equals("Fail")) {
            throw new Exception("Crash");
        }
        contacts.put(p.name(), p.email());
        System.out.println("Added " + p);
    }

    private void onGetMessage(GetMessage p) {
        String email = contacts.get(p.name());
        getSender().tell(email==null?"NON TROVATO":email, self());
        System.out.println("Sent " + email);
    }

    // Used by the props static class to create the actor
    static Props props() {
        return Props.create(ContactActor.class);
    }

}
