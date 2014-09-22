package com.joraph.schema;

public class SimilarBook {

	private String id;
	private String reason;
	private String bookId;
	private String similarBookId;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public SimilarBook setId(String id) {
		this.id = id;
		return this;
	}
	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}
	/**
	 * @param reason the reason to set
	 */
	public SimilarBook setReason(String reason) {
		this.reason = reason;
		return this;
	}
	/**
	 * @return the bookId
	 */
	public String getBookId() {
		return bookId;
	}
	/**
	 * @param bookId the bookId to set
	 */
	public SimilarBook setBookId(String bookId) {
		this.bookId = bookId;
		return this;
	}
	/**
	 * @return the similarBookId
	 */
	public String getSimilarBookId() {
		return similarBookId;
	}
	/**
	 * @param similarBookId the similarBookId to set
	 */
	public SimilarBook setSimilarBookId(String similarBookId) {
		this.similarBookId = similarBookId;
		return this;
	}

}
