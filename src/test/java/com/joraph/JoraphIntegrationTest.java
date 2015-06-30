package com.joraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.joraph.debug.JoraphDebug;
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
import com.joraph.schema.User;
import com.joraph.schema.UserFollow;

public class JoraphIntegrationTest
		extends AbstractJoraphTest {

	private JoraphContext context;
	private Map<Object, Object> values;

	@Before
	public void setUp()
			throws Exception {
		super.setupSchema();
		initDb();
		context = new JoraphContext(getSchema());
		context.addLoader(Author.class, 		new TestLoader(Author.class));
		context.addLoader(Book.class, 			new TestLoader(Book.class));
		context.addLoader(Checkout.class, 		new TestLoader(Checkout.class));
		context.addLoader(Genre.class, 			new TestLoader(Genre.class));
		context.addLoader(Library.class, 		new TestLoader(Library.class));
		context.addLoader(User.class, 			new TestLoader(User.class));
		context.addLoader(SimilarBook.class, 	new TestLoader(SimilarBook.class));
		context.addLoader(FeaturedBook.class,   new TestLoader(Book.class));
		context.addLoader(UserFollow.class,   	new TestLoader(UserFollow.class));
	}

	@After
	public void tearDown()
			throws Exception {
		super.tearDownSchema();
		context = null;
	}

	@Test
	public void testSimpleObjectGraph() {

		Book book1 = (Book)values.get("book1");

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
	public void testDeepObjectsAreLoaded() {

		Book book1 = (Book)values.get("book2"); // BOOK 2 !

		ObjectGraph objectGraph = context.execute(Book.class, Arrays.asList(book1));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertEquals("user1", objectGraph.get(User.class, "user1").getId());

	}

	@Test
	public void testSimpleObjectGraph_MultipleRoots() {

		Book book1 = (Book)values.get("book1");
		User user1 = (User)values.get("user1");

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

		Book book1 = (Book)values.get("book1");
		Book book2 = (Book)values.get("book2");

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

		assertNull(objectGraph.get(Library.class, "library2"));

	}

	@Test
	public void testCheckout() {

		Checkout checkout1 = (Checkout)values.get("checkout1");

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

	}

	@Test
	public void testFeaturedBook() throws Exception {
		final FeaturedBook featuredBook1 = (FeaturedBook)values.get("featuredBook1");

		final ObjectGraph objectGraph = context.execute(FeaturedBook.class, featuredBook1);

		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());
	}

	@Test
	public void testFeatureBookWhenFeaturedBooksIsNull() throws Exception {
		final ObjectGraph objectGraph = context.execute(FeaturedBook.class, (Iterable<FeaturedBook>)null);

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

		User user1 = (User)values.get("user1");

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
				new BasicCompositeKey("user3", "user2"));
		assertNotNull(objectGraph);
		assertTrue(objectGraph.has(User.class, "user1"));
		assertTrue(objectGraph.has(User.class, "user2"));
		assertTrue(objectGraph.has(User.class, "user3"));
		
		assertTrue(objectGraph.has(UserFollow.class, new BasicCompositeKey("user1", "user2")));
		assertTrue(objectGraph.has(UserFollow.class, new BasicCompositeKey("user2", "user1")));
		assertTrue(objectGraph.has(UserFollow.class, new BasicCompositeKey("user3", "user1")));
		assertTrue(objectGraph.has(UserFollow.class, new BasicCompositeKey("user2", "user3")));

	}

	@Test
	public void testDeepForeignKeys()
			throws Exception {

		getSchema().getEntityDescriptor(Checkout.class)
			.addForeignKey("metaData.librarianUserId", User.class);

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

		Book book1 = (Book)values.get("book1");
		context.execute(Book.class, Arrays.asList(book1));
		assertFalse(JoraphDebug.hasDebugInfo());
	}

	@Test
	public void testDebugInfoIsCollected() {

		JoraphDebug.startDebug();

		Book book1 = (Book)values.get("book1");
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

		Book book1 = (Book)values.get("book1");
		Author author1 = (Author)values.get("author1");
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

	public class TestLoader
			implements EntityLoader<Object> {

		private Class<?> entityClass;

		public TestLoader(Class<?> entityClass) {
			this.entityClass = entityClass;
		}

		@Override
		public List<Object> load(Iterable<?> ids) {
			List<Object> ret = new ArrayList<Object>();
			for (Object id : ids) {
				if (!values.containsKey(id)) {
					continue;
				}
				Object val = values.get(id);
				if (entityClass.isInstance(val)) {
					ret.add(values.get(id));
				}
			}
			return ret;
		}
		
	}

	@SuppressWarnings("serial")
	private void initDb() {
		values = new HashMap<Object, Object>() {{

			put("author1", new Author()
				.setId("author1")
				.setName("Author 1"));
			put("author2", new Author()
				.setId("author2")
				.setName("Author 2"));
			put("author3", new Author()
				.setId("author3")
				.setName("Author 3"));

			put("user1", new User()
				.setId("user1")
				.setName("User 1")
				.setFavoriteAuthorIds(Lists.newArrayList("author1", "author2")));
			put("user2", new User()
				.setId("user2")
				.setName("User 2"));
			put("user3", new User()
				.setId("user3")
				.setName("User 3")
				.setFavoriteAuthorIds(Lists.newArrayList("author3", "author1")));

			put(new BasicCompositeKey("user1", "user2"),
					new UserFollow("user1", "user2"));
			put(new BasicCompositeKey("user2", "user1"),
					new UserFollow("user2", "user1"));
			put(new BasicCompositeKey("user3", "user1"),
					new UserFollow("user3", "user1"));
			put(new BasicCompositeKey("user2", "user3"),
					new UserFollow("user2", "user3"));

			put("genre1", new Genre()
				.setId("genre1")
				.setName("Genre 1"));
			put("genre2", new Genre()
				.setId("genre2")
				.setName("Genre 2"));
			put("genre3", new Genre()
				.setId("genre3")
				.setName("Genre 3"));

			put("library1", new Library()
				.setId("library1")
				.setName("Library 1")
				.setLibrarianUserId("user3"));
			put("library2", new Library()
				.setId("library2")
				.setName("Library 2")
				.setLibrarianUserId("user1"));

			put("book1", new Book()
				.setId("book1")
				.setName("Book 1")
				.setAuthorId("author3")
				.setGenreId("genre2")
				.setLibraryId("library1"));
			put("book2", new Book()
				.setId("book2")
				.setName("Book 2")
				.setAuthorId("author2")
				.setCoAuthorId("author3")
				.setGenreId("genre1")
				.setLibraryId("library1")
				.setRating(new Rating()
					.setRating(4.20f)
					.setUserId("user1")));

			put("checkout1", new Checkout()
				.setId("checkout1")
				.setBookId("book2")
				.setLibraryId("library1")
				.setUserId("user2"));

			put("featuredBook1", new FeaturedBook()
					.setId("featuredBook1")
					.setBookId("book1")
					.setFeaturedById("user3"));

		}};
	}


}
