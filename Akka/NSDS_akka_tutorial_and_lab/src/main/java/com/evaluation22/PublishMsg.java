package com.evaluation22;

public class PublishMsg {

	private String topic;
	private String value;
	
	public PublishMsg (String topic, String value) {
		this.topic = topic;
		this.value = value;
	}

	public String getTopic() {
		return topic;
	}

	public String getValue() {
		return value;
	}
}
