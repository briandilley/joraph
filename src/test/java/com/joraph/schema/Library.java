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
	public void setId(String id) {
		this.id = id;
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
	public void setName(String name) {
		this.name = name;
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
	public void setLibrarianUserId(String librarianUserId) {
		this.librarianUserId = librarianUserId;
	}
}
