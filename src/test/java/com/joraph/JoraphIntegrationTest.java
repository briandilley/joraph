package com.joraph;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.joraph.schema.Author;
import com.joraph.schema.Book;
import com.joraph.schema.Checkout;
import com.joraph.schema.Genre;
import com.joraph.schema.Library;
import com.joraph.schema.SimilarBook;
import com.joraph.schema.User;

public class JoraphIntegrationTest
		extends AbstractJoraphTest {

	private JoraphContext context;
	private Map<String, Object> values;

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

		Results results = context.execute(Book.class, Arrays.asList(book1));
		assertNotNull(results);

		assertNotNull(results.get(Book.class, "book1"));
		assertEquals("book1", results.get(Book.class, "book1").getId());

		assertNotNull(results.get(Author.class, "author3"));
		assertEquals("author3", results.get(Author.class, "author3").getId());

		assertNotNull(results.get(Genre.class, "genre2"));
		assertEquals("genre2", results.get(Genre.class, "genre2").getId());

		assertNotNull(results.get(Library.class, "library1"));
		assertEquals("library1", results.get(Library.class, "library1").getId());

		assertNull(results.get(Library.class, "library2"));

	}

	@Test
	public void testLessSimpleObjectGraph() {

		Book book1 = (Book)values.get("book1");
		Book book2 = (Book)values.get("book2");

		Results results = context.execute(Book.class, Arrays.asList(book1, book2));
		assertNotNull(results);

		// book1

		assertNotNull(results.get(Book.class, "book1"));
		assertEquals("book1", results.get(Book.class, "book1").getId());

		assertNotNull(results.get(Author.class, "author3"));
		assertEquals("author3", results.get(Author.class, "author3").getId());

		assertNotNull(results.get(Genre.class, "genre2"));
		assertEquals("genre2", results.get(Genre.class, "genre2").getId());

		assertNotNull(results.get(Library.class, "library1"));
		assertEquals("library1", results.get(Library.class, "library1").getId());

		assertNotNull(results.get(User.class, "user3"));
		assertEquals("user3", results.get(User.class, "user3").getId());

		assertNull(results.get(Library.class, "library2"));

		// book2

		assertNotNull(results.get(Book.class, "book2"));
		assertEquals("book2", results.get(Book.class, "book2").getId());

		assertNotNull(results.get(Author.class, "author2"));
		assertEquals("author2", results.get(Author.class, "author2").getId());

		assertNotNull(results.get(Author.class, "author3"));
		assertEquals("author3", results.get(Author.class, "author3").getId());

		assertNotNull(results.get(Genre.class, "genre1"));
		assertEquals("genre1", results.get(Genre.class, "genre1").getId());

		assertNotNull(results.get(Library.class, "library1"));
		assertEquals("library1", results.get(Library.class, "library1").getId());

		assertNotNull(results.get(User.class, "user3"));
		assertEquals("user3", results.get(User.class, "user3").getId());

		assertNull(results.get(Library.class, "library2"));

	}

	@Test
	public void testCheckout() {

		Checkout checkout1 = (Checkout)values.get("checkout1");

		Results results = context.execute(Checkout.class, Arrays.asList(checkout1));
		assertNotNull(results);

		// checkout1

		assertNotNull(results.get(Book.class, "book2"));
		assertEquals("book2", results.get(Book.class, "book2").getId());

		assertNotNull(results.get(User.class, "user2"));
		assertEquals("user2", results.get(User.class, "user2").getId());

		assertNotNull(results.get(Library.class, "library1"));
		assertEquals("library1", results.get(Library.class, "library1").getId());

		assertNotNull(results.get(Author.class, "author2"));
		assertEquals("author2", results.get(Author.class, "author2").getId());

		assertNotNull(results.get(Author.class, "author3"));
		assertEquals("author3", results.get(Author.class, "author3").getId());

		assertNotNull(results.get(Genre.class, "genre1"));
		assertEquals("genre1", results.get(Genre.class, "genre1").getId());

		assertNotNull(results.get(User.class, "user3"));
		assertEquals("user3", results.get(User.class, "user3").getId());


	}

	public class TestLoader
			implements EntityLoader<Object> {

		private Class<?> entityClass;

		public TestLoader(Class<?> entityClass) {
			this.entityClass = entityClass;
		}

		@Override
		public List<Object> load(Iterable<Serializable> ids) {
			List<Object> ret = new ArrayList<Object>();
			for (Serializable id : ids) {
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
		values = new HashMap<String, Object>() {{

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
				.setName("User 1"));
			put("user2", new User()
				.setId("user2")
				.setName("User 2"));
			put("user3", new User()
				.setId("user3")
				.setName("User 3"));

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
				.setLibraryId("library1"));

			put("checkout1", new Checkout()
				.setId("checkout1")
				.setBookId("book2")
				.setLibraryId("library1")
				.setUserId("user2"));
		}};
	}

}