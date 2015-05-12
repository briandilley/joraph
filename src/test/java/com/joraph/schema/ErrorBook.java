package com.joraph.schema;

public class ErrorBook {
	private String bookId;
	private String anotherErrorBookId;

	public String getBookId() {
		return bookId;
	}

	public ErrorBook setBookId(String bookId) {
		this.bookId = bookId;
		return this;
	}

	public String getAnotherErrorBookId() {
		return anotherErrorBookId;
	}

	public ErrorBook setAnotherErrorBookId(String anotherErrorBookId) {
		this.anotherErrorBookId = anotherErrorBookId;
		return this;
	}
}
