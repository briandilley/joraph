package com.joraph.schema;

public class AuthorMessage
		extends Message<AuthorMessage> {

	private String authorId;

	public String getAuthorId() {
		return authorId;
	}

	public AuthorMessage setAuthorId(String authorId) {
		this.authorId = authorId;
		return this;
	}

}
