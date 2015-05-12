package com.joraph.schema;

public class Rating {
	
	public Float rating;
	public String userId;
	public Float getRating() {
		return rating;
	}
	public Rating setRating(Float rating) {
		this.rating = rating;
		return this;
	}
	public String getUserId() {
		return userId;
	}
	public Rating setUserId(String userId) {
		this.userId = userId;
		return this;
	}

}
