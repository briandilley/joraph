package com.joraph;

import com.google.common.base.Converter;
import com.google.common.base.Joiner;
import com.joraph.schema.Author;
import com.joraph.schema.BasicCompositeKey;
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
import com.joraph.schema.UserFollow;

public abstract class AbstractJoraphTest {

	public static Converter<Object[], String> STRING_CONCAT_CONVERTER
			= new Converter<Object[], String>() {
				@Override
				protected String doForward(Object[] a) {
					return Joiner.on("|").join(a);
				}
				@Override
				protected Object[] doBackward(String b) {
					return b.split("|");
				}
			};

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
		user.addForeignKey("favoriteAuthorIds", Author.class);
		
		EntityDescriptor library = schema.addEntityDescriptor(Library.class);
		library.setPrimaryKey("id");
		library.addForeignKey("librarianUserId", User.class);
		
		EntityDescriptor book = schema.addEntityDescriptor(Book.class);
		book.setPrimaryKey("id");
		book.addForeignKey("authorId", Author.class);
		book.addForeignKey("coAuthorId", Author.class);
		book.addForeignKey("genreId", Genre.class);
		book.addForeignKey("libraryId", Library.class);
		book.addForeignKey("rating.userId", User.class);
		
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

		/* follows */
		schema.addEntityDescriptor(UserFollow.class)
			.setPrimaryKey(BasicCompositeKey.CONVERTER, "fromUserId", "toUserId")
			.addForeignKey("fromUserId", User.class)
			.addForeignKey("toUserId", User.class);

		/* this entity should have no EntityLoader defined */
		EntityDescriptor errorBook = schema.addEntityDescriptor(ErrorBook.class);
		errorBook.setPrimaryKey("bookId");
		errorBook.addForeignKey("anotherErrorBookId", ErrorBook.class);

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
