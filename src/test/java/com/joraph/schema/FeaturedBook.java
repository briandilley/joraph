package com.joraph.schema;

public class FeaturedBook {
	private String id;
	private String bookId;
	private String featuredById;

	public FeaturedBook setId(String id) {
		this.id = id;
		return this;
	}

	public String getId() {
		return id;
	}

	public String getBookId() {
		return bookId;
	}

	public FeaturedBook setBookId(String bookId) {
		this.bookId = bookId;
		return this;
	}

	public String getFeaturedById() {
		return featuredById;
	}

	public FeaturedBook setFeaturedById(String featuredById) {
		this.featuredById = featuredById;
		return this;
	}

}
