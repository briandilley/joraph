package com.joraph.schema;

public class Library {
	private String id;
	private String name;
	private String librarianUserId;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public Library setId(String id) {
		this.id = id;
		return this;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public Library setName(String name) {
		this.name = name;
		return this;
	}
	/**
	 * @return the librarianUserId
	 */
	public String getLibrarianUserId() {
		return librarianUserId;
	}
	/**
	 * @param librarianUserId the librarianUserId to set
	 */
	public Library setLibrarianUserId(String librarianUserId) {
		this.librarianUserId = librarianUserId;
		return this;
	}
}
