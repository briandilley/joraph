package com.joraph;

import com.joraph.schema.Author;
import com.joraph.schema.Book;
import com.joraph.schema.Checkout;
import com.joraph.schema.EntityDescriptor;
import com.joraph.schema.ErrorBook;
import com.joraph.schema.FeaturedBook;
import com.joraph.schema.Genre;
import com.joraph.schema.Library;
import com.joraph.schema.Schema;
import com.joraph.schema.SimilarBook;
import com.joraph.schema.User;

public abstract class AbstractJoraphTest {

	private Schema schema;

	public void setupSchema()
		throws Exception {
		schema = new Schema();

		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey("id");
		
		EntityDescriptor genre = schema.addEntityDescriptor(Genre.class);
		genre.setPrimaryKey("id");
		
		EntityDescriptor user = schema.addEntityDescriptor(User.class);
		user.setPrimaryKey("id");
		
		EntityDescriptor library = schema.addEntityDescriptor(Library.class);
		library.setPrimaryKey("id");
		library.addForeignKey("librarianUserId", User.class);
		
		EntityDescriptor book = schema.addEntityDescriptor(Book.class);
		book.setPrimaryKey("id");
		book.addForeignKey("authorId", Author.class);
		book.addForeignKey("coAuthorId", Author.class);
		book.addForeignKey("genreId", Genre.class);
		book.addForeignKey("libraryId", Library.class);
		
		EntityDescriptor checkout = schema.addEntityDescriptor(Checkout.class);
		checkout.setPrimaryKey("id");
		checkout.addForeignKey("userId", User.class);
		checkout.addForeignKey("libraryId", Library.class);
		checkout.addForeignKey("bookId", Book.class);
		
		EntityDescriptor similarBook = schema.addEntityDescriptor(SimilarBook.class);
		similarBook.setPrimaryKey("id");
		similarBook.addForeignKey("bookId", Book.class);
		similarBook.addForeignKey("similarBookId", Book.class);

		EntityDescriptor featuredBook = schema.addEntityDescriptor(FeaturedBook.class);
		/* purposefully set this way */
		featuredBook.setPrimaryKey("bookId");
		featuredBook.addForeignKey("bookId", Book.class);
		featuredBook.addForeignKey("featuredById", User.class);

		/* this entity should have no EntityLoader defined */
		EntityDescriptor errorBook = schema.addEntityDescriptor(ErrorBook.class);
		errorBook.setPrimaryKey("bookId");

		schema.validate();
	}

	public void tearDownSchema()
		throws Exception {
		schema = null;
	}

	protected Schema getSchema() {
		return schema;
	}
}
