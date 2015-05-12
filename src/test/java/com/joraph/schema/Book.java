package com.joraph.schema;

public class Book {

	private String id;
	private String authorId;
	private String coAuthorId;
	private String genreId;
	private String libraryId;
	private String name;
	private String isbn;
	private Integer numberOfPages;
	private Rating rating;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public Book setId(String id) {
		this.id = id;
		return this;
	}
	/**
	 * @return the authorId
	 */
	public String getAuthorId() {
		return authorId;
	}
	/**
	 * @param authorId the authorId to set
	 */
	public Book setAuthorId(String authorId) {
		this.authorId = authorId;
		return this;
	}
	/**
	 * @return the coAuthorId
	 */
	public String getCoAuthorId() {
		return coAuthorId;
	}
	/**
	 * @param coAuthorId the coAuthorId to set
	 */
	public Book setCoAuthorId(String coAuthorId) {
		this.coAuthorId = coAuthorId;
		return this;
	}
	/**
	 * @return the genreId
	 */
	public String getGenreId() {
		return genreId;
	}
	/**
	 * @param genreId the genreId to set
	 */
	public Book setGenreId(String genreId) {
		this.genreId = genreId;
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
	public Book setLibraryId(String libraryId) {
		this.libraryId = libraryId;
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
	public Book setName(String name) {
		this.name = name;
		return this;
	}
	/**
	 * @return the isbn
	 */
	public String getIsbn() {
		return isbn;
	}
	/**
	 * @param isbn the isbn to set
	 */
	public Book setIsbn(String isbn) {
		this.isbn = isbn;
		return this;
	}
	/**
	 * @return the numberOfPages
	 */
	public Integer getNumberOfPages() {
		return numberOfPages;
	}
	/**
	 * @param numberOfPages the numberOfPages to set
	 */
	public Book setNumberOfPages(Integer numberOfPages) {
		this.numberOfPages = numberOfPages;
		return this;
	}

	public Rating getRating() {
		return rating;
	}

	public Book setRating(Rating rating) {
		this.rating = rating;
		return this;
	}

}
