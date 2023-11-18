package com.evaluation22;

import akka.actor.ActorRef;

public class SubscribeMsg {

	private int key;
	private String topic;
	private ActorRef sender;
	
	public SubscribeMsg (String topic, ActorRef sender) {
		this.key = this.hashCode();
		this.topic = topic;
		this.sender = sender;
	}

	public String getTopic() {
		return topic;
	}

	public int getKey() {
		return key;
	}

	public ActorRef getSender() {
		return sender;
	}

}
