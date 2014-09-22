package com.joraph.schema;

public class Genre {

	private String id;
	private String name;
	private String description;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public Genre setId(String id) {
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
	public Genre setName(String name) {
		this.name = name;
		return this;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public Genre setDescription(String description) {
		this.description = description;
		return this;
	}

}
