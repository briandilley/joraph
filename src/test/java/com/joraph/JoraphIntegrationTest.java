package com.joraph;

import static com.joraph.schema.PropertyDescriptorChain.newChain;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.joraph.debug.JoraphDebug;
import com.joraph.plan.ExecutionPlan;
import com.joraph.schema.Author;
import com.joraph.schema.BasicCompositeKey;
import com.joraph.schema.Book;
import com.joraph.schema.BookMessage;
import com.joraph.schema.Checkout;
import com.joraph.schema.CheckoutMetaData;
import com.joraph.schema.FeaturedBook;
import com.joraph.schema.Genre;
import com.joraph.schema.Library;
import com.joraph.schema.Message;
import com.joraph.schema.MessagePair;
import com.joraph.schema.Schema;
import com.joraph.schema.UnknownEntityDescriptorException;
import com.joraph.schema.User;
import com.joraph.schema.UserEx;
import com.joraph.schema.UserFollow;
import com.joraph.schema.UserMessage;

public class JoraphIntegrationTest
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

	@Test
	public void testSimpleObjectGraph() {

		Book book1 = testDb.get(Book.class, "book1");
		User user1 = testDb.get(User.class, "user1");

		Query q = new Query(Book.class, User.class)
				.withRootObject(book1, user1);

		System.out.println(context.plan(q));

		ObjectGraph objectGraph = context.execute(q);
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

		UserFollow user3_user4 = testDb.get(UserFollow.class, new BasicCompositeKey("user3", "user4"));
		UserFollow user3_user2 = testDb.get(UserFollow.class, new BasicCompositeKey("user3", "user2"));

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

		schema.getEntityDescriptors(Book.class).findFirstByEntityClass(Book.class).get().setGraphKey(String.class);
		schema.getEntityDescriptors(Author.class).findFirstByEntityClass(Author.class).get().setGraphKey(Integer.class);

		Book book1 = testDb.get(Book.class, "book1");

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
	public void testPolyMorphicLoading() {

		ExecutionPlan plan = context.plan(CollectionUtil.asSet(MessagePair.class));
		assertNotNull(plan);
		System.out.println(plan.toString());

		MessagePair messagePair = testDb.get(MessagePair.class, new BasicCompositeKey("usermessage1", "bookmessage4"));

		ObjectGraph objectGraph = context.execute(MessagePair.class, messagePair);
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(UserMessage.class, "usermessage1"));
		assertEquals("usermessage1", objectGraph.get(UserMessage.class, "usermessage1").getId());

		assertNotNull(objectGraph.get(BookMessage.class, "bookmessage4"));
		assertEquals("bookmessage4", objectGraph.get(BookMessage.class, "bookmessage4").getId());

		assertNotNull(objectGraph.get(Message.class, "usermessage1"));
		assertEquals("usermessage1", objectGraph.get(Message.class, "usermessage1").getId());

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertEquals("user1", objectGraph.get(User.class, "user1").getId());

		assertNotNull(objectGraph.get(Book.class, "book2"));
		assertEquals("book2", objectGraph.get(Book.class, "book2").getId());

		assertNotNull(objectGraph.get(Author.class, "author3"));
		assertEquals("author3", objectGraph.get(Author.class, "author3").getId());
	}

	@Test
	public void testDeepObjectsAreLoaded() {

		Book book1 = testDb.get(Book.class, "book2"); // BOOK 2 !

		ObjectGraph objectGraph = context.execute(Book.class, Arrays.asList(book1));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertEquals("user1", objectGraph.get(User.class, "user1").getId());

	}

	@Test
	public void testSimpleObjectGraph_MultipleRoots() {

		Book book1 = testDb.get(Book.class, "book1");
		User user1 = testDb.get(User.class, "user1");

		ObjectGraph objectGraph = context.execute(new Query()
				.withRootEntities(asList(book1, user1)));
		assertNotNull(objectGraph);

		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());

		assertNotNull(objectGraph.get(User.class, "user1"));
		assertEquals("user1", objectGraph.get(User.class, "user1").getId());

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

		Book book1 = testDb.get(Book.class, "book1");
		Book book2 = testDb.get(Book.class, "book2");

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

		Checkout checkout1 = testDb.get(Checkout.class, "checkout1");

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
		final FeaturedBook featuredBook1 = testDb.get(FeaturedBook.class, "book1");

		final ObjectGraph objectGraph = context.execute(FeaturedBook.class, featuredBook1);

		assertNotNull(objectGraph.get(FeaturedBook.class, "book1"));
		assertNotNull(objectGraph.get(Book.class, "book1"));
		assertEquals("book1", objectGraph.get(Book.class, "book1").getId());
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

		User user1 = testDb.get(User.class, "user1");

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

		schema.getEntityDescriptors(Checkout.class)
			.findFirstByEntityClass(Checkout.class)
			.get()
			.addForeignKey(User.class, newChain(Checkout::getMetaData)
					.andThen(CheckoutMetaData::getLibrarianUserId)
					.build());

		schema.validate();
		assertTrue(schema.isValidated());

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

		Book book1 = testDb.get(Book.class, "book1");
		context.execute(Book.class, Arrays.asList(book1));
		assertFalse(JoraphDebug.hasDebugInfo());
	}

	@Test
	public void testDebugInfoIsCollected() {

		JoraphDebug.startDebug();

		Book book1 = testDb.get(Book.class, "book1");
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

		Book book1 = testDb.get(Book.class, "book1");
		Author author1 = testDb.get(Author.class, "author1");
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

}
