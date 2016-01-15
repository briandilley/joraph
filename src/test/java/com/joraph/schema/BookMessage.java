package com.joraph.schema;

public class BookMessage
		extends Message<BookMessage> {

	private String bookId;

	public String getBookId() {
		return bookId;
	}

	public BookMessage setBookId(String bookId) {
		this.bookId = bookId;
		return this;
	}

}
