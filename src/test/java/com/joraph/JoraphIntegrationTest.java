package com.joraph;

import static com.joraph.schema.PropertyDescriptorChain.newChain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.joraph.debug.JoraphDebug;
import com.joraph.loader.EntityLoader;
import com.joraph.schema.Author;
import com.joraph.schema.BasicCompositeKey;
import com.joraph.schema.Book;
import com.joraph.schema.Checkout;
import com.joraph.schema.CheckoutMetaData;
import com.joraph.schema.FeaturedBook;
import com.joraph.schema.Genre;
import com.joraph.schema.Library;
import com.joraph.schema.Rating;
import com.joraph.schema.SimilarBook;
import com.joraph.schema.UnknownEntityDescriptorException;
import com.joraph.schema.User;
import com.joraph.schema.UserEx;
import com.joraph.schema.UserFollow;

public class JoraphIntegrationTest
		extends AbstractJoraphTest {

	private JoraphContext context;
	private ObjectGraph values;

	@Before
	public void setUp()
			throws Exception {
		super.setupSchema();
		initDb();
		context = new JoraphContext(getSchema());
		context.addLoader(Author.class, 		new TestLoader<>(Author.class));
		context.addLoader(Book.class, 			new TestLoader<>(Book.class));
		context.addLoader(Checkout.class, 		new TestLoader<>(Checkout.class));
		context.addLoader(Genre.class, 			new TestLoader<>(Genre.class));
		context.addLoader(Library.class, 		new TestLoader<>(Library.class));
		context.addLoader(User.class, 			new TestLoader<>(User.class));
		context.addLoader(SimilarBook.class, 	new TestLoader<>(SimilarBook.class));
		context.addLoader(FeaturedBook.class,   new TestLoader<>(Book.class));
		context.addLoader(UserFollow.class,   	new TestLoader<>(UserFollow.class));
	}

	@After
	public void tearDown()
			throws Exception {
		super.tearDownSchema();
		context = null;
	}

	@Test
	public void testSimpleObjectGraph() {

		Book book1 = values.get(Book.class, "book1");

		ObjectGraph objectGraph = context.execute(Book.class, Arrays.asList(book1));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());

		assertNotNull(objectGraph.get(Author.class, "author3"));
		assertEquals("author3", objectGraph.get(Author.class, "author3").getId());

		assertNotNull(objectGraph.get(Genre.class, "genre2"));
		assertEquals("genre2", objectGraph.get(Genre.class, "genre2").getId());

		assertNotNull(objectGraph.get(Library.class, "library1"));
		assertEquals("library1", objectGraph.get(Library.class, "library1").getId());

		assertNull(objectGraph.get(Library.class, "library2"));

	}

	@Test
	public void testLoadFromGraphTypeKey() {

		UserFollow user3_user4 = values.get(UserFollow.class, new BasicCompositeKey("user3", "user4"));
		UserFollow user3_user2 = values.get(UserFollow.class, new BasicCompositeKey("user3", "user2"));

		ObjectGraph objectGraph = context.execute(UserFollow.class,
				Arrays.asList(user3_user4, user3_user2));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(UserEx.class, "user3"));
		assertEquals("user3", objectGraph.get(UserEx.class, "user3").getId());

		assertNotNull(objectGraph.get(UserEx.class, "user4"));
		assertEquals("user4", objectGraph.get(UserEx.class, "user4").getId());

	}

	@Test
	public void testSimpleObjectGraphWithDifferentGraphTypeKeys() {

		getSchema().getEntityDescriptor(Book.class).setGraphKey(String.class);
		getSchema().getEntityDescriptor(Author.class).setGraphKey(Integer.class);

		Book book1 = values.get(Book.class, "book1");

		ObjectGraph objectGraph = context.execute(Book.class, Arrays.asList(book1));
		assertNotNull(objectGraph);
		Map<Class<?>, Map<Object, Object>> map = objectGraph.getResults();
		assertNotNull(map);

		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());
		assertNotNull(map.get(String.class));
		assertNotNull(map.get(String.class).get("book1"));

		assertNotNull(objectGraph.get(Author.class, "author3"));
		assertEquals("author3", objectGraph.get(Author.class, "author3").getId());
		assertNotNull(map.get(Integer.class));
		assertNotNull(map.get(Integer.class).get("author3"));

		assertNotNull(objectGraph.get(Genre.class, "genre2"));
		assertEquals("genre2", objectGraph.get(Genre.class, "genre2").getId());

		assertNotNull(objectGraph.get(Library.class, "library1"));
		assertEquals("library1", objectGraph.get(Library.class, "library1").getId());

		assertNull(objectGraph.get(Library.class, "library2"));

	}

	@Test
	public void testDeepObjectsAreLoaded() {

		Book book1 = values.get(Book.class, "book2"); // BOOK 2 !

		ObjectGraph objectGraph = context.execute(Book.class, Arrays.asList(book1));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertEquals("user1", objectGraph.get(User.class, "user1").getId());

	}

	@Test
	public void testSimpleObjectGraph_MultipleRoots() {

		Book book1 = values.get(Book.class, "book1");
		User user1 = values.get(User.class, "user1");

		ObjectGraph objectGraph = context.executeForObjects(Arrays.asList(book1, user1));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertEquals("user1", objectGraph.get(User.class, "user1").getId());

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertEquals("author1", objectGraph.get(Author.class, "author1").getId());
		assertEquals("author2", objectGraph.get(Author.class, "author2").getId());

		assertNotNull(objectGraph.get(Author.class, "author3"));
		assertEquals("author3", objectGraph.get(Author.class, "author3").getId());

		assertNotNull(objectGraph.get(Genre.class, "genre2"));
		assertEquals("genre2", objectGraph.get(Genre.class, "genre2").getId());

		assertNotNull(objectGraph.get(Library.class, "library1"));
		assertEquals("library1", objectGraph.get(Library.class, "library1").getId());

		assertNull(objectGraph.get(Library.class, "library2"));

	}

	@Test
	public void testLessSimpleObjectGraph() {

		Book book1 = values.get(Book.class, "book1");
		Book book2 = values.get(Book.class, "book2");

		ObjectGraph objectGraph = context.execute(Book.class, Arrays.asList(book1, book2));
		assertNotNull(objectGraph);

		// book1

		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());

		assertNotNull(objectGraph.get(Author.class, "author3"));
		assertEquals("author3", objectGraph.get(Author.class, "author3").getId());

		assertNotNull(objectGraph.get(Genre.class, "genre2"));
		assertEquals("genre2", objectGraph.get(Genre.class, "genre2").getId());

		assertNotNull(objectGraph.get(Library.class, "library1"));
		assertEquals("library1", objectGraph.get(Library.class, "library1").getId());

		assertNotNull(objectGraph.get(User.class, "user3"));
		assertEquals("user3", objectGraph.get(User.class, "user3").getId());

		assertNotNull(objectGraph.get(UserEx.class, "user3"));
		assertEquals("user3", objectGraph.get(UserEx.class, "user3").getId());

		assertNull(objectGraph.get(Library.class, "library2"));

		// book2

		assertNotNull(objectGraph.get(Book.class, "book2"));
		assertEquals("book2", objectGraph.get(Book.class, "book2").getId());

		assertNotNull(objectGraph.get(Author.class, "author2"));
		assertEquals("author2", objectGraph.get(Author.class, "author2").getId());

		assertNotNull(objectGraph.get(Author.class, "author3"));
		assertEquals("author3", objectGraph.get(Author.class, "author3").getId());

		assertNotNull(objectGraph.get(Genre.class, "genre1"));
		assertEquals("genre1", objectGraph.get(Genre.class, "genre1").getId());

		assertNotNull(objectGraph.get(Library.class, "library1"));
		assertEquals("library1", objectGraph.get(Library.class, "library1").getId());

		assertNotNull(objectGraph.get(User.class, "user3"));
		assertEquals("user3", objectGraph.get(User.class, "user3").getId());

		assertNotNull(objectGraph.get(UserEx.class, "user3"));
		assertEquals("user3", objectGraph.get(UserEx.class, "user3").getId());

		assertNull(objectGraph.get(Library.class, "library2"));

	}

	@Test
	public void testCheckout() {

		Checkout checkout1 = values.get(Checkout.class, "checkout1");

		ObjectGraph objectGraph = context.execute(Checkout.class, Arrays.asList(checkout1));
		assertNotNull(objectGraph);

		// checkout1

		assertNotNull(objectGraph.get(Book.class, "book2"));
		assertEquals("book2", objectGraph.get(Book.class, "book2").getId());

		assertNotNull(objectGraph.get(User.class, "user2"));
		assertEquals("user2", objectGraph.get(User.class, "user2").getId());

		assertNotNull(objectGraph.get(Library.class, "library1"));
		assertEquals("library1", objectGraph.get(Library.class, "library1").getId());

		assertNotNull(objectGraph.get(Author.class, "author2"));
		assertEquals("author2", objectGraph.get(Author.class, "author2").getId());

		assertNotNull(objectGraph.get(Author.class, "author3"));
		assertEquals("author3", objectGraph.get(Author.class, "author3").getId());

		assertNotNull(objectGraph.get(Genre.class, "genre1"));
		assertEquals("genre1", objectGraph.get(Genre.class, "genre1").getId());

		assertNotNull(objectGraph.get(User.class, "user3"));
		assertEquals("user3", objectGraph.get(User.class, "user3").getId());

		assertNotNull(objectGraph.get(UserEx.class, "user3"));
		assertEquals("user3", objectGraph.get(UserEx.class, "user3").getId());

	}

	@Test
	public void testFeaturedBook() throws Exception {
		final FeaturedBook featuredBook1 = values.get(FeaturedBook.class, "featuredBook1");

		final ObjectGraph objectGraph = context.execute(FeaturedBook.class, featuredBook1);

		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());
	}

	@Test
	public void testFeatureBookWhenFeaturedBooksIsNull() throws Exception {
		final ObjectGraph objectGraph = context.execute(FeaturedBook.class, (Collection<FeaturedBook>)null);

		assertNull(objectGraph.get(Book.class, "book1"));
	}

	@Test
	public void testFeatureBookWhenFeaturedBooksIsEmpty() throws Exception {
		final ObjectGraph objectGraph = context.execute(FeaturedBook.class, Collections.<FeaturedBook>emptyList());

		assertNull(objectGraph.get(Book.class, "book1"));
	}

	@Test(expected = UnknownEntityDescriptorException.class)
	public void testUnconfiguredClassLoader() throws Exception {
		context.execute(Object.class, new Object());
	}

	@Test
	public void testSupplementExistingGraph() {

		User user1 = values.get(User.class, "user1");

		ObjectGraph objectGraph = context.execute(User.class, user1);
		assertNotNull(objectGraph);
		assertTrue(objectGraph.has(User.class, "user1"));
		assertFalse(objectGraph.has(User.class, "user2"));
		assertFalse(objectGraph.has(User.class, "user3"));

		objectGraph = context.supplement(objectGraph, UserFollow.class,
				new BasicCompositeKey("user1", "user2"),
				new BasicCompositeKey("user1", "user3"),
				new BasicCompositeKey("user2", "user1"),
				new BasicCompositeKey("user2", "user3"),
				new BasicCompositeKey("user3", "user1"),
				new BasicCompositeKey("user3", "user2"),
				new BasicCompositeKey("user3", "user7"));
		assertNotNull(objectGraph);
		assertTrue(objectGraph.has(User.class, "user1"));
		assertTrue(objectGraph.has(User.class, "user2"));
		assertTrue(objectGraph.has(User.class, "user3"));
		assertTrue(objectGraph.has(UserEx.class, "user3"));
		assertFalse(objectGraph.has(User.class, "user4"));
		assertFalse(objectGraph.has(UserEx.class, "user4"));

		// user1 follows: user2
		// user2 follows: user1, user3
		// user3 follows: user1

		assertTrue(objectGraph.has(UserFollow.class, new BasicCompositeKey("user1", "user2")));
		assertTrue(objectGraph.has(UserFollow.class, new BasicCompositeKey("user2", "user1")));
		assertTrue(objectGraph.has(UserFollow.class, new BasicCompositeKey("user2", "user3")));
		assertTrue(objectGraph.has(UserFollow.class, new BasicCompositeKey("user3", "user1")));
		assertTrue(objectGraph.has(UserFollow.class, new BasicCompositeKey("user3", "user2")));
		assertFalse(objectGraph.has(UserFollow.class, new BasicCompositeKey("user1", "user3")));
		assertFalse(objectGraph.has(UserFollow.class, new BasicCompositeKey("user3", "user7")));

	}

	@Test
	public void testDeepForeignKeys()
			throws Exception {

		getSchema().getEntityDescriptor(Checkout.class)
			.addForeignKey(User.class, newChain(Checkout::getMetaData)
					.andThen(CheckoutMetaData::getLibrarianUserId)
					.build());

		getSchema().validate();
		assertTrue(getSchema().isValidated());

		 Checkout checkout = new Checkout()
		 	.setId("checkout1")
		 	.setUserId("user1")
		 	.setMetaData(new CheckoutMetaData()
		 		.setLibrarianUserId("user2"));

		 ObjectGraph objectGraph = context.execute(Checkout.class, checkout);
		 assertNotNull(objectGraph);
		 assertNotNull(objectGraph.get(User.class, "user1"));
		 assertNotNull(objectGraph.get(User.class, "user2"));

	}

	@Test
	public void testDebugInfoIsNotCollected() {

		Book book1 = values.get(Book.class, "book1");
		context.execute(Book.class, Arrays.asList(book1));
		assertFalse(JoraphDebug.hasDebugInfo());
	}

	@Test
	public void testDebugInfoIsCollected() {

		JoraphDebug.startDebug();

		Book book1 = values.get(Book.class, "book1");
		ObjectGraph objectGraph = context.execute(Book.class, Arrays.asList(book1));

		assertTrue(JoraphDebug.hasDebugInfo());
		assertEquals(1, JoraphDebug.getDebugInfo().getObjectGraphs().size());
		assertEquals(1, JoraphDebug.getDebugInfo().getExecutionPlans().size());
		assertTrue(JoraphDebug.getDebugInfo().getObjectGraphs().contains(objectGraph));

		JoraphDebug.finishDebug();
		assertFalse(JoraphDebug.hasDebugInfo());
	}

	@Test
	public void testDebugInfoIsCollectedTwice() {

		JoraphDebug.startDebug();

		Book book1 = values.get(Book.class, "book1");
		Author author1 = values.get(Author.class, "author1");
		ObjectGraph objectGraph = context.execute(Book.class, Arrays.asList(book1));
		ObjectGraph objectGraph2 = context.execute(Author.class, Arrays.asList(author1));

		assertTrue(JoraphDebug.hasDebugInfo());
		assertEquals(2, JoraphDebug.getDebugInfo().getObjectGraphs().size());
		assertEquals(2, JoraphDebug.getDebugInfo().getExecutionPlans().size());
		assertTrue(JoraphDebug.getDebugInfo().getObjectGraphs().contains(objectGraph));
		assertTrue(JoraphDebug.getDebugInfo().getObjectGraphs().contains(objectGraph2));

		JoraphDebug.finishDebug();
		assertFalse(JoraphDebug.hasDebugInfo());
	}

	/* TODO fix https://github.com/briandilley/joraph/issues/7 and re-enable, or rewrite with 2 entities */
//	@Test(expected = UnconfiguredLoaderException.class)
//	public void testErrorBookWhenAttemptingToLoad() throws Exception {
//		context.execute(ErrorBook.class, new ErrorBook().setAnotherErrorBookId("another-error-book-id"));
//	}

	public class TestLoader<T>
			implements EntityLoader<T> {

		private Class<T> entityClass;

		public TestLoader(Class<T> entityClass) {
			this.entityClass = entityClass;
		}

		@Override
		public List<T> load(Iterable<?> ids) {
			Collection<?> idCollection = StreamSupport.stream(ids.spliterator(), false)
					.collect(Collectors.toList());
			return values.getList(entityClass, idCollection);
		}
		
	}

	private void initDb() {
		values = new ObjectGraph() {{

			addResult(Author.class, "author1", new Author()
				.setId("author1")
				.setName("Author 1"));
			addResult(Author.class, "author2", new Author()
				.setId("author2")
				.setName("Author 2"));
			addResult(Author.class, "author3", new Author()
				.setId("author3")
				.setName("Author 3"));

			addResult(User.class, "user1", new User()
				.setId("user1")
				.setName("User 1")
				.setFavoriteAuthorIds(CollectionUtil.asList("author1", "author2")));
			addResult(User.class, "user2", new User()
				.setId("user2")
				.setName("User 2"));
			addResult(User.class, "user3", new UserEx()
				.setId("user3")
				.setName("User 3")
				.setFavoriteAuthorIds(CollectionUtil.asList("author3", "author1")));
			addResult(User.class, "user4", new UserEx()
				.setId("user4")
				.setName("User 4"));
			
			// user1 follows: user2
			// user2 follows: user1, user3
			// user3 follows: user1

			addResult(UserFollow.class, new BasicCompositeKey("user1", "user2"),
					new UserFollow("user1", "user2"));
			addResult(UserFollow.class, new BasicCompositeKey("user2", "user1"),
					new UserFollow("user2", "user1"));
			addResult(UserFollow.class, new BasicCompositeKey("user3", "user1"),
					new UserFollow("user3", "user1"));
			addResult(UserFollow.class, new BasicCompositeKey("user2", "user3"),
					new UserFollow("user2", "user3"));
			addResult(UserFollow.class, new BasicCompositeKey("user3", "user4"),
					new UserFollow("user3", "user4"));
			addResult(UserFollow.class, new BasicCompositeKey("user3", "user2"),
					new UserFollow("user3", "user2"));

			addResult(Genre.class, "genre1", new Genre()
				.setId("genre1")
				.setName("Genre 1"));
			addResult(Genre.class, "genre2", new Genre()
				.setId("genre2")
				.setName("Genre 2"));
			addResult(Genre.class, "genre3", new Genre()
				.setId("genre3")
				.setName("Genre 3"));

			addResult(Library.class, "library1", new Library()
				.setId("library1")
				.setName("Library 1")
				.setLibrarianUserId("user3"));
			addResult(Library.class, "library2", new Library()
				.setId("library2")
				.setName("Library 2")
				.setLibrarianUserId("user1"));

			addResult(Book.class, "book1", new Book()
				.setId("book1")
				.setName("Book 1")
				.setAuthorId("author3")
				.setGenreId("genre2")
				.setLibraryId("library1"));
			addResult(Book.class, "book2", new Book()
				.setId("book2")
				.setName("Book 2")
				.setAuthorId("author2")
				.setCoAuthorId("author3")
				.setGenreId("genre1")
				.setLibraryId("library1")
				.setRating(new Rating()
					.setRating(4.20f)
					.setUserId("user1")));

			addResult(Checkout.class, "checkout1", new Checkout()
				.setId("checkout1")
				.setBookId("book2")
				.setLibraryId("library1")
				.setUserId("user2"));

			addResult(FeaturedBook.class, "featuredBook1", new FeaturedBook()
				.setId("featuredBook1")
				.setBookId("book1")
				.setFeaturedById("user3"));

		}};
	}


}
