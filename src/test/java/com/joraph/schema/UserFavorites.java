package com.joraph.schema;

import java.util.Set;

public class UserFavorites {

	private String userId;
	private Set<String> authorIds;
	private Set<String> libraryIds;

	public String getUserId() {
		return userId;
	}

	public UserFavorites setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public Set<String> getAuthorIds() {
		return authorIds;
	}

	public UserFavorites setAuthorIds(Set<String> authorIds) {
		this.authorIds = authorIds;
		return this;
	}

	public Set<String> getLibraryIds() {
		return libraryIds;
	}

	public UserFavorites setLibraryIds(Set<String> libraryIds) {
		this.libraryIds = libraryIds;
		return this;
	}

}
