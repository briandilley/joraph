package com.joraph.schema;

import java.util.Date;

public class Checkout {

	private String id;
	private Date date;
	private String libraryId;
	private String userId;
	private String bookId;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public Checkout setId(String id) {
		this.id = id;
		return this;
	}
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public Checkout setDate(Date date) {
		this.date = date;
		return this;
	}
	/**
	 * @return the libraryId
	 */
	public String getLibraryId() {
		return libraryId;
	}
	/**
	 * @param libraryId the libraryId to set
	 */
	public Checkout setLibraryId(String libraryId) {
		this.libraryId = libraryId;
		return this;
	}
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public Checkout setUserId(String userId) {
		this.userId = userId;
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
	public Checkout setBookId(String bookId) {
		this.bookId = bookId;
		return this;
	}

}
