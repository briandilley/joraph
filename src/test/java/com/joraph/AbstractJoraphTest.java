package com.joraph;

import static com.joraph.schema.PropertyDescriptorChain.newChain;

import com.joraph.schema.Author;
import com.joraph.schema.BasicCompositeKey;
import com.joraph.schema.Book;
import com.joraph.schema.Checkout;
import com.joraph.schema.ErrorBook;
import com.joraph.schema.FeaturedBook;
import com.joraph.schema.Genre;
import com.joraph.schema.Library;
import com.joraph.schema.PropertyDescriptorChain;
import com.joraph.schema.Rating;
import com.joraph.schema.Schema;
import com.joraph.schema.SimilarBook;
import com.joraph.schema.User;
import com.joraph.schema.UserEx;
import com.joraph.schema.UserFollow;

public abstract class AbstractJoraphTest {

	private Schema schema;

	public void setupSchema()
		throws Exception {
		schema = new Schema();

		schema.addEntityDescriptor(Author.class)
			.setPrimaryKey(Author::getId);
		
		schema.addEntityDescriptor(Genre.class)
			.setPrimaryKey(Genre::getId);

		schema.addEntityDescriptor(User.class)
			.setGraphKey(User.class)
			.setPrimaryKey(User::getId)
			.addForeignKey(Author.class, User::getFavoriteAuthorIds);

		schema.addEntityDescriptor(UserEx.class)
			.setGraphKey(User.class)
			.setPrimaryKey(UserEx::getId)
			.addForeignKey(Author.class, UserEx::getFavoriteAuthorIds);

		schema.addEntityDescriptor(Library.class)
			.setPrimaryKey(Library::getId)
			.addForeignKey(User.class, Library::getLibrarianUserId);
		
		schema.addEntityDescriptor(Book.class)
			.setPrimaryKey(Book::getId)
			.addForeignKey(Author.class, Book::getAuthorId)
			.addForeignKey(Author.class, Book::getCoAuthorId)
			.addForeignKey(Genre.class, Book::getGenreId)
			.addForeignKey(Library.class, Book::getLibraryId)
			.addForeignKey(User.class, newChain(Book::getRating)
				.andThen(Rating::getUserId)
				.build());
		
		schema.addEntityDescriptor(Checkout.class)
			.setPrimaryKey(Checkout::getId)
			.addForeignKey(User.class, Checkout::getUserId)
			.addForeignKey(Library.class, Checkout::getLibraryId)
			.addForeignKey(Book.class, Checkout::getBookId);
		
		schema.addEntityDescriptor(SimilarBook.class)
			.setPrimaryKey(SimilarBook::getId)
			.addForeignKey(Book.class, SimilarBook::getBookId)
			.addForeignKey(Book.class, SimilarBook::getSimilarBookId);

		schema.addEntityDescriptor(FeaturedBook.class)
		/* purposefully set this way */
			.setPrimaryKey(FeaturedBook::getBookId)
			.addForeignKey(Book.class, FeaturedBook::getBookId)
			.addForeignKey(User.class, FeaturedBook::getFeaturedById);

		/* follows */
		schema.addEntityDescriptor(UserFollow.class)
			.setPrimaryKey(BasicCompositeKey.CONVERTER,
					new PropertyDescriptorChain<>(UserFollow::getFromUserId),
					new PropertyDescriptorChain<>(UserFollow::getToUserId))
			.addForeignKey(User.class, UserFollow::getFromUserId)
			.addForeignKey(User.class, UserFollow::getToUserId);

		/* this entity should have no EntityLoader defined */
		schema.addEntityDescriptor(ErrorBook.class)
			.setPrimaryKey(ErrorBook::getBookId)
			.addForeignKey(ErrorBook.class, ErrorBook::getAnotherErrorBookId);

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
