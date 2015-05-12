package com.joraph.schema;

public class UserFollow {

	private String fromUserId;
	private String toUserId;

	public UserFollow() {
		
	}

	public UserFollow(String fromuserId, String toUserId) {
		this.fromUserId = fromuserId;
		this.toUserId = toUserId;
	}

	/**
	 * @return the fromUserId
	 */
	public String getFromUserId() {
		return fromUserId;
	}

	/**
	 * @param fromUserId the fromUserId to set
	 */
	public UserFollow withFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
		return this;
	}

	/**
	 * @return the toUserId
	 */
	public String getToUserId() {
		return toUserId;
	}

	/**
	 * @param toUserId the toUserId to set
	 */
	public UserFollow withToUserId(String toUserId) {
		this.toUserId = toUserId;
		return this;
	}

	/**
	 * @param fromUserId the fromUserId to set
	 */
	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	/**
	 * @param toUserId the toUserId to set
	 */
	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

}
