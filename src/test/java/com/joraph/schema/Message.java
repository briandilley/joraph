package com.joraph.schema;

@SuppressWarnings("unchecked")
public abstract class Message<T extends Message<?>> {
	private String id;
	private String payload;

	public String getId() {
		return id;
	}

	public T setId(String id) {
		this.id = id;
		return (T)this;
	}

	public String getPayload() {
		return payload;
	}

	public T setPayload(String payload) {
		this.payload = payload;
		return (T)this;
	}

}
