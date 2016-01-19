package com.joraph;

import static com.joraph.schema.PropertyDescriptorChain.buildChain;
import static com.joraph.schema.PropertyDescriptorChain.newChain;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.joraph.loader.EntityLoaderContext;
import com.joraph.loader.LoaderFunction;
import com.joraph.schema.Author;
import com.joraph.schema.AuthorMessage;
import com.joraph.schema.BasicCompositeKey;
import com.joraph.schema.Book;
import com.joraph.schema.BookMessage;
import com.joraph.schema.Checkout;
import com.joraph.schema.ErrorBook;
import com.joraph.schema.FeaturedBook;
import com.joraph.schema.Genre;
import com.joraph.schema.LatestMessage;
import com.joraph.schema.Library;
import com.joraph.schema.Message;
import com.joraph.schema.MessagePair;
import com.joraph.schema.PropertyDescriptorChain;
import com.joraph.schema.Rating;
import com.joraph.schema.UserFavorites;
import com.joraph.schema.Schema;
import com.joraph.schema.SimilarBook;
import com.joraph.schema.User;
import com.joraph.schema.UserEx;
import com.joraph.schema.UserFollow;
import com.joraph.schema.UserMessage;

public abstract class AbstractJoraphTest {

	protected <A, I, R> LoaderFunction<A, I, R> loaderFor(ObjectGraph testDb, Class<R> entityClass) {
		return (a, ids) -> load(testDb, entityClass, ids);
	}

	protected <T> List<T> load(ObjectGraph testDb, Class<T> entityClass, Iterable<?> ids) {
		Collection<?> idCollection = StreamSupport.stream(ids.spliterator(), false)
				.collect(Collectors.toList());
		return testDb.getList(entityClass, idCollection);
	}

	public void setupLoaders(ObjectGraph testDb, JoraphContext context) {
		EntityLoaderContext loaderContext = context.getLoaderContext();
		loaderContext.addLoader(Author.class, 		this.loaderFor(testDb, Author.class));
		loaderContext.addLoader(Book.class, 		this.loaderFor(testDb, Book.class));
		loaderContext.addLoader(Checkout.class, 	this.loaderFor(testDb, Checkout.class));
		loaderContext.addLoader(Genre.class, 		this.loaderFor(testDb, Genre.class));
		loaderContext.addLoader(Library.class, 		this.loaderFor(testDb, Library.class));
		loaderContext.addLoader(User.class, 		this.loaderFor(testDb, User.class));
		loaderContext.addLoader(SimilarBook.class, 	this.loaderFor(testDb, SimilarBook.class));
		loaderContext.addLoader(FeaturedBook.class, this.loaderFor(testDb, FeaturedBook.class));
		loaderContext.addLoader(UserFollow.class,  	this.loaderFor(testDb, UserFollow.class));
		loaderContext.addLoader(Message.class,  	this.loaderFor(testDb, Message.class));
	}

	public Schema setupSchema(Schema schema)
		throws Exception {

		schema.addEntityDescriptor(Author.class)
			.setPrimaryKey(Author::getId);
		
		schema.addEntityDescriptor(Genre.class)
			.setPrimaryKey(Genre::getId);

		schema.addEntityDescriptor(User.class)
			.setGraphKey(User.class)
			.setPrimaryKey(User::getId);

		schema.addEntityDescriptor(UserFavorites.class)
			.setPrimaryKey(UserFavorites::getUserId)
			.addForeignKey(User.class, UserFavorites::getUserId)
			.addForeignKey(User.class)
				.withAccessor(UserFavorites::getUserId)
				.add()
			.addForeignKey(Author.class)
				.withAccessor(UserFavorites::getAuthorIds)
				.withPredicate(TestArgs.class, TestArgs::isLoadFavoriteAuthors)
				.add()
			.addForeignKey(Library.class)
				.withAccessor(UserFavorites::getLibraryIds)
				.withPredicate(TestArgs.class, TestArgs::isLoadFavoriteLibraries)
				.add();

		schema.addEntityDescriptor(UserEx.class)
			.setGraphKey(User.class)
			.setPrimaryKey(UserEx::getId);

		schema.addEntityDescriptor(Library.class)
			.setPrimaryKey(Library::getId)
			.addForeignKey(User.class, Library::getLibrarianUserId);
		
		schema.addEntityDescriptor(Book.class)
			.setPrimaryKey(Book::getId)
			.addForeignKey(Author.class, Book::getAuthorId)
			.addForeignKey(Author.class, Book::getCoAuthorId)
			.addForeignKey(Genre.class, Book::getGenreId)
			.addForeignKey(Library.class, Book::getLibraryId)
			.addForeignKey(Library.class)
				.withAccessor(Book::getLibraryId)
				.add()
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


		schema.addEntityDescriptor(LatestMessage.class)
			.setPrimaryKey(LatestMessage::getId)
			.addForeignKey(Message.class, LatestMessage::getLatestMessageId);

		schema.addEntityDescriptor(MessagePair.class)
			.setPrimaryKey(buildChain(MessagePair::getLeft), buildChain(MessagePair::getRight))
			.addForeignKey(Message.class, MessagePair::getLeft)
			.addForeignKey(Message.class, MessagePair::getRight);

		schema.addEntityDescriptor(UserMessage.class)
			.setGraphKey(Message.class)
			.setPrimaryKey(UserMessage::getId)
			.addForeignKey(User.class, UserMessage::getUserId);

		schema.addEntityDescriptor(BookMessage.class)
			.setGraphKey(Message.class)
			.setPrimaryKey(BookMessage::getId)
			.addForeignKey(Book.class, BookMessage::getBookId);

		schema.addEntityDescriptor(AuthorMessage.class)
			.setGraphKey(Message.class)
			.setPrimaryKey(AuthorMessage::getId)
			.addForeignKey(Author.class, AuthorMessage::getAuthorId);

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
		return schema;
	}

	protected ObjectGraph setupTestDb(ObjectGraph testDb) {

		testDb.addResult(Author.class, "author1", new Author()
			.setId("author1")
			.setName("Author 1"));
		testDb.addResult(Author.class, "author2", new Author()
			.setId("author2")
			.setName("Author 2"));
		testDb.addResult(Author.class, "author3", new Author()
			.setId("author3")
			.setName("Author 3"));

		testDb.addResult(User.class, "user1", new User()
			.setId("user1")
			.setName("User 1"));
		testDb.addResult(User.class, "user2", new User()
			.setId("user2")
			.setName("User 2"));
		testDb.addResult(User.class, "user3", new UserEx()
			.setId("user3")
			.setName("User 3"));
		testDb.addResult(User.class, "user4", new UserEx()
			.setId("user4")
			.setName("User 4"));

		testDb.addResult(LatestMessage.class, "latestMessage1", new LatestMessage()
			.setId("latestMessage1")
			.setLatestMessageId("usermessage1"));

		testDb.addResult(MessagePair.class, new BasicCompositeKey("usermessage1", "bookmessage4"), new MessagePair()
			.setLeft("usermessage1")
			.setRight("bookmessage4"));

		testDb.addResult(Message.class, "usermessage1", new UserMessage()
			.setId("usermessage1")
			.setPayload("message 1")
			.setUserId("user1"));
		testDb.addResult(Message.class, "usermessage2", new UserMessage()
			.setId("usermessage2")
			.setPayload("message 2")
			.setUserId("user2"));
		testDb.addResult(Message.class, "authormessage3", new AuthorMessage()
			.setId("authormessage3")
			.setPayload("message 3")
			.setAuthorId("author2"));
		testDb.addResult(Message.class, "bookmessage4", new BookMessage()
			.setId("bookmessage4")
			.setPayload("message 4")
			.setBookId("book2"));
		testDb.addResult(Message.class, "bookmessage5", new BookMessage()
			.setId("bookmessage5")
			.setPayload("message 5")
			.setBookId("book1"));
		
		// user1 follows: user2
		// user2 follows: user1, user3
		// user3 follows: user1

		testDb.addResult(UserFollow.class, new BasicCompositeKey("user1", "user2"),
			new UserFollow("user1", "user2"));
		testDb.addResult(UserFollow.class, new BasicCompositeKey("user2", "user1"),
			new UserFollow("user2", "user1"));
		testDb.addResult(UserFollow.class, new BasicCompositeKey("user3", "user1"),
			new UserFollow("user3", "user1"));
		testDb.addResult(UserFollow.class, new BasicCompositeKey("user2", "user3"),
			new UserFollow("user2", "user3"));
		testDb.addResult(UserFollow.class, new BasicCompositeKey("user3", "user4"),
			new UserFollow("user3", "user4"));
		testDb.addResult(UserFollow.class, new BasicCompositeKey("user3", "user2"),
			new UserFollow("user3", "user2"));

		testDb.addResult(Genre.class, "genre1", new Genre()
			.setId("genre1")
			.setName("Genre 1"));
		testDb.addResult(Genre.class, "genre2", new Genre()
			.setId("genre2")
			.setName("Genre 2"));
		testDb.addResult(Genre.class, "genre3", new Genre()
			.setId("genre3")
			.setName("Genre 3"));

		testDb.addResult(Library.class, "library1", new Library()
			.setId("library1")
			.setName("Library 1")
			.setLibrarianUserId("user3"));
		testDb.addResult(Library.class, "library2", new Library()
			.setId("library2")
			.setName("Library 2")
			.setLibrarianUserId("user1"));

		testDb.addResult(Book.class, "book1", new Book()
			.setId("book1")
			.setName("Book 1")
			.setAuthorId("author3")
			.setGenreId("genre2")
			.setLibraryId("library1"));
		testDb.addResult(Book.class, "book2", new Book()
			.setId("book2")
			.setName("Book 2")
			.setAuthorId("author2")
			.setCoAuthorId("author3")
			.setGenreId("genre1")
			.setLibraryId("library1")
			.setRating(new Rating()
				.setRating(4.20f)
				.setUserId("user1")));

		testDb.addResult(UserFavorites.class, "user1", new UserFavorites()
				.setUserId("user1")
				.setAuthorIds(CollectionUtil.asSet("author1", "author2"))
				.setLibraryIds(CollectionUtil.asSet("library1")));

		testDb.addResult(UserFavorites.class, "user2", new UserFavorites()
				.setUserId("user2")
				.setAuthorIds(CollectionUtil.asSet("author2", "author3"))
				.setLibraryIds(CollectionUtil.asSet("library2")));

		testDb.addResult(UserFavorites.class, "user3", new UserFavorites()
				.setUserId("user2")
				.setLibraryIds(CollectionUtil.asSet("library1", "library2")));

		testDb.addResult(Checkout.class, "checkout1", new Checkout()
			.setId("checkout1")
			.setBookId("book2")
			.setLibraryId("library1")
			.setUserId("user2"));

		testDb.addResult(FeaturedBook.class, "book1", new FeaturedBook()
			.setBookId("book1")
			.setFeaturedById("user3"));

		return testDb;
	}
}
