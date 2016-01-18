package com.joraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.joraph.schema.Author;
import com.joraph.schema.Book;
import com.joraph.schema.FeaturedBook;
import com.joraph.schema.Library;
import com.joraph.schema.Schema;
import com.joraph.schema.User;
import com.joraph.schema.UserFavorites;

public class LoaderIntegrationTest
		extends AbstractJoraphTest {

	private Schema schema;
	private ObjectGraph testDb;
	private JoraphContext context;

	@Before
	public void setUp()
			throws Exception {
		schema		= setupSchema(new Schema());
		testDb		= setupTestDb(new ObjectGraph());
		context 	= new JoraphContext(schema);
		setupLoaders(testDb, context);
	}

	@After
	public void tearDown()
			throws Exception {
		schema = null;
		testDb = null;
		context = null;
	}

	private List<Book> loadBooks(String arg1, Iterable<?> ids) {
		return load(testDb, Book.class, ids);
	}

	private List<Author> loadAuthors(Integer arg1, Iterable<?> ids) {
		return load(testDb, Author.class, ids);
	}

	@Test
	public void testLoadWithPredicateLoader() {

		TestArgs arguments = new TestArgs();
		arguments.setLoadFavoriteAuthors(true);
		arguments.setLoadFavoriteLibraries(true);

		UserFavorites userFavorites1 = testDb.get(UserFavorites.class, "user1");
		assertNotNull(userFavorites1);
		UserFavorites userFavorites2 = testDb.get(UserFavorites.class, "user2");
		assertNotNull(userFavorites2);

		ObjectGraph objectGraph = context.execute(new Query(UserFavorites.class)
				.withRootObject(userFavorites1, userFavorites2)
				.withArguments(arguments));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(UserFavorites.class, "user1"));
		assertNotNull(objectGraph.get(UserFavorites.class, "user2"));

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertNotNull(objectGraph.get(User.class, "user2"));
		assertNotNull(objectGraph.get(User.class, "user3"));

		assertNotNull(objectGraph.get(Author.class, "author1"));
		assertNotNull(objectGraph.get(Author.class, "author2"));
		assertNotNull(objectGraph.get(Author.class, "author3"));

		assertNotNull(objectGraph.get(Library.class, "library1"));
		assertNotNull(objectGraph.get(Library.class, "library2"));


		arguments.setLoadFavoriteAuthors(false);
		arguments.setLoadFavoriteLibraries(true);

		objectGraph = context.execute(new Query(UserFavorites.class)
				.withRootObject(userFavorites1, userFavorites2)
				.withArguments(arguments));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(UserFavorites.class, "user1"));
		assertNotNull(objectGraph.get(UserFavorites.class, "user2"));

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertNotNull(objectGraph.get(User.class, "user2"));
		assertNotNull(objectGraph.get(User.class, "user3"));

		assertNull(objectGraph.get(Author.class, "author1"));
		assertNull(objectGraph.get(Author.class, "author2"));
		assertNull(objectGraph.get(Author.class, "author3"));

		assertNotNull(objectGraph.get(Library.class, "library1"));
		assertNotNull(objectGraph.get(Library.class, "library2"));


		arguments.setLoadFavoriteAuthors(false);
		arguments.setLoadFavoriteLibraries(false);

		objectGraph = context.execute(new Query(UserFavorites.class)
				.withRootObject(userFavorites1, userFavorites2)
				.withArguments("Test"));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(UserFavorites.class, "user1"));
		assertNotNull(objectGraph.get(UserFavorites.class, "user2"));

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertNotNull(objectGraph.get(User.class, "user2"));
		assertNull(objectGraph.get(User.class, "user3"));

		assertNull(objectGraph.get(Author.class, "author1"));
		assertNull(objectGraph.get(Author.class, "author2"));
		assertNull(objectGraph.get(Author.class, "author3"));

		assertNull(objectGraph.get(Library.class, "library1"));
		assertNull(objectGraph.get(Library.class, "library2"));

	}

	@Test
	public void testLoadWithLoadersThatRequireAdditionalArguments() {

		TestArgs arguments = new TestArgs();

		context.getLoaderContext().addLoader(Book.class, this::loadBooks, TestArgs.class, TestArgs::incrementAndGetArg1);
		context.getLoaderContext().addLoader(Author.class, this::loadAuthors, TestArgs.class, TestArgs::incrementAndGetArg2);

		FeaturedBook featuredBook1 = testDb.get(FeaturedBook.class, "book1");
		assertNotNull(featuredBook1);

		ObjectGraph objectGraph = context.execute(new Query(FeaturedBook.class)
				.withRootObject(featuredBook1)
				.withArguments(arguments));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(FeaturedBook.class, "book1"));
		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());
		assertTrue(arguments.getArg1().get() > 0);
		assertTrue(arguments.getArg2().get() > 0);

	}

	@Test
	public void testLoadWithLoadersThatRequireAdditionalArgumentsPassesNullWhenUnavailable() {

		AtomicBoolean bookLoaderCalled = new AtomicBoolean(false);
		AtomicBoolean authorLoaderCalled = new AtomicBoolean(false);

		context.getLoaderContext()
			.addLoader(Book.class)
				.withArgument(TestArgs.class, TestArgs::incrementAndGetArg1)
				.withFunction((String arg, Iterable<?> ids) -> {
					bookLoaderCalled.set(true);
					assertNull(arg);
					return loadBooks(null, ids);
				})
				.add()
			.addLoader(Author.class)
				.withArgument(TestArgs.class, TestArgs::incrementAndGetArg2)
				.withFunction((Integer arg, Iterable<?> ids) -> {
					authorLoaderCalled.set(true);
					assertNull(arg);
					return loadAuthors(null, ids);
				})
				.add();

		FeaturedBook featuredBook1 = testDb.get(FeaturedBook.class, "book1");
		assertNotNull(featuredBook1);

		ObjectGraph objectGraph = context.execute(new Query(FeaturedBook.class)
				.withRootObject(featuredBook1));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(FeaturedBook.class, "book1"));
		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());
		assertTrue(bookLoaderCalled.get());
		assertTrue(authorLoaderCalled.get());

	}

}
