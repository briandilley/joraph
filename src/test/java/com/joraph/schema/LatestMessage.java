package com.joraph.schema;

public class LatestMessage {

	private String id;
	private String latestMessageId;

	public String getId() {
		return id;
	}

	public LatestMessage setId(String id) {
		this.id = id;
		return this;
	}

	public String getLatestMessageId() {
		return latestMessageId;
	}

	public LatestMessage setLatestMessageId(String latestMessageId) {
		this.latestMessageId = latestMessageId;
		return this;
	}

}
