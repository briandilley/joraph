package com.joraph.schema;

public class FeaturedBook {

	private String bookId;
	private String featuredById;

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
