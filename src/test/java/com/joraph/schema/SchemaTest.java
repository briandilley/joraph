package com.joraph.schema;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SchemaTest {

	private Schema schema;

	@Before
	public void setUp()
		throws Exception {
		schema = new Schema();
	}

	@After
	public void tearDown()
		throws Exception {
		schema = null;
	}

	@Test(expected=MissingPrimaryKeyException.class)
	public void testMissingPrimaryKeyException() {
		schema.addEntityDescriptor(Author.class);
		schema.validate();
	}

	@Test(expected=UnknownFKException.class)
	public void testUnknownFKException()
		throws Exception {
		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey(Author::getId);
		author.addForeignKey(Book.class, Author::getId);
		schema.validate();
	}

	@Test
	public void testValidateSingleEntity()
		throws Exception {
		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey(Author::getId);
		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}

	@Test
	public void testLargeSchema()
		throws Exception {
		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey(Author::getId);

		EntityDescriptor genre = schema.addEntityDescriptor(Genre.class);
		genre.setPrimaryKey(Genre::getId);

		EntityDescriptor book = schema.addEntityDescriptor(Book.class);
		book.setPrimaryKey(Book::getId);
		book.addForeignKey(Author.class, Book::getAuthorId);
		book.addForeignKey(Genre.class, Book::getGenreId);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}

	@Test
	public void testDirty()
		throws Exception {
		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey(Author::getId);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());

		EntityDescriptor genre = schema.addEntityDescriptor(Genre.class);
		genre.setPrimaryKey(Genre::getId);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}


}
