package com.joraph;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.joraph.schema.Author;
import com.joraph.schema.BasicCompositeKey;
import com.joraph.schema.Book;
import com.joraph.schema.Checkout;
import com.joraph.schema.EntityDescriptor;
import com.joraph.schema.ErrorBook;
import com.joraph.schema.FeaturedBook;
import com.joraph.schema.Genre;
import com.joraph.schema.Library;
import com.joraph.schema.PropertyDescriptorChain;
import com.joraph.schema.Rating;
import com.joraph.schema.Schema;
import com.joraph.schema.SimilarBook;
import com.joraph.schema.User;
import com.joraph.schema.UserFollow;

public abstract class AbstractJoraphTest {

	public static Function<Object[], String> STRING_CONCAT_CONVERTER = (a) -> Stream.of(a).map(String.class::cast).collect(Collectors.joining("|"));
	public static Function<String, Object[]> STRING_CONCAT_CONVERTER_R = (a) -> a.split("|");

	private Schema schema;

	public void setupSchema()
		throws Exception {
		schema = new Schema();

		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey(Author::getId);
		
		EntityDescriptor genre = schema.addEntityDescriptor(Genre.class);
		genre.setPrimaryKey(Genre::getId);

		EntityDescriptor user = schema.addEntityDescriptor(User.class);
		user.setPrimaryKey(User::getId);
		user.addForeignKey(Author.class, User::getFavoriteAuthorIds);

		EntityDescriptor library = schema.addEntityDescriptor(Library.class);
		library.setPrimaryKey(Library::getId);
		library.addForeignKey(User.class, Library::getLibrarianUserId);
		
		EntityDescriptor book = schema.addEntityDescriptor(Book.class);
		book.setPrimaryKey(Book::getId);
		book.addForeignKey(Author.class, Book::getAuthorId);
		book.addForeignKey(Author.class, Book::getCoAuthorId);
		book.addForeignKey(Genre.class, Book::getGenreId);
		book.addForeignKey(Library.class, Book::getLibraryId);
		book.addForeignKey(User.class, new PropertyDescriptorChain.Builder()
				.add(Book::getRating)
				.add(Rating::getUserId)
				.build());
		
		EntityDescriptor checkout = schema.addEntityDescriptor(Checkout.class);
		checkout.setPrimaryKey(Checkout::getId);
		checkout.addForeignKey(User.class, Checkout::getUserId);
		checkout.addForeignKey(Library.class, Checkout::getLibraryId);
		checkout.addForeignKey(Book.class, Checkout::getBookId);
		
		EntityDescriptor similarBook = schema.addEntityDescriptor(SimilarBook.class);
		similarBook.setPrimaryKey(SimilarBook::getId);
		similarBook.addForeignKey(Book.class, SimilarBook::getBookId);
		similarBook.addForeignKey(Book.class, SimilarBook::getSimilarBookId);

		EntityDescriptor featuredBook = schema.addEntityDescriptor(FeaturedBook.class);
		/* purposefully set this way */
		featuredBook.setPrimaryKey(FeaturedBook::getBookId);
		featuredBook.addForeignKey(Book.class, FeaturedBook::getBookId);
		featuredBook.addForeignKey(User.class, FeaturedBook::getFeaturedById);

		/* follows */
		schema.addEntityDescriptor(UserFollow.class)
			.setPrimaryKey(BasicCompositeKey.CONVERTER,
					new PropertyDescriptorChain(UserFollow::getFromUserId),
					new PropertyDescriptorChain(UserFollow::getToUserId))
			.addForeignKey(User.class, UserFollow::getFromUserId)
			.addForeignKey(User.class, UserFollow::getToUserId);

		/* this entity should have no EntityLoader defined */
		EntityDescriptor errorBook = schema.addEntityDescriptor(ErrorBook.class);
		errorBook.setPrimaryKey(ErrorBook::getBookId);
		errorBook.addForeignKey(ErrorBook.class, ErrorBook::getAnotherErrorBookId);

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
