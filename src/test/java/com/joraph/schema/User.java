package com.joraph.schema;

public class User {

	private String id;
	private String name;
	private String referedByUserId;

	public String getId() {
		return id;
	}

	public User setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public User setName(String name) {
		this.name = name;
		return this;
	}

	public String getReferedByUserId() {
		return referedByUserId;
	}

	public User setReferedByUserId(String referedByUserId) {
		this.referedByUserId = referedByUserId;
		return this;
	}

}
