package com.joraph.schema;

public class UserMessage
		extends Message<UserMessage> {

	private String userId;

	public String getUserId() {
		return userId;
	}

	public UserMessage setUserId(String userId) {
		this.userId = userId;
		return this;
	}

}
