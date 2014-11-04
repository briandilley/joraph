package com.joraph.schema;

import java.util.List;

public class User {

	private String id;
	private String name;
	private List<String> favoriteAuthorIds;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public User setId(String id) {
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
	public User setName(String name) {
		this.name = name;
		return this;
	}
	/**
	 * @return the favoriteAuthorIds
	 */
	public List<String> getFavoriteAuthorIds() {
		return favoriteAuthorIds;
	}
	/**
	 * @param favoriteAuthorIds the favoriteAuthorIds to set
	 */
	public User setFavoriteAuthorIds(List<String> favoriteAuthorIds) {
		this.favoriteAuthorIds = favoriteAuthorIds;
		return this;
	}
}
