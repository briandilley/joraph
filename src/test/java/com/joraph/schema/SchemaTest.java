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
		schema.addEntityDescriptor(Author.class)
				.setPrimaryKey(Author::getId)
				.addForeignKey(Book.class, Author::getId);
		schema.validate();
	}

	@Test
	public void testValidateSingleEntity()
		throws Exception {
		schema.addEntityDescriptor(Author.class)
				.setPrimaryKey(Author::getId);
		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}

	@Test
	public void testLargeSchema()
		throws Exception {
		schema.addEntityDescriptor(Author.class)
			.setPrimaryKey(Author::getId);

		schema.addEntityDescriptor(Genre.class)
			.setPrimaryKey(Genre::getId);

		schema.addEntityDescriptor(Book.class)
			.setPrimaryKey(Book::getId)
			.addForeignKey(Author.class, Book::getAuthorId)
			.addForeignKey(Genre.class, Book::getGenreId);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}

	@Test
	public void testDirty()
		throws Exception {
		schema.addEntityDescriptor(Author.class)
			.setPrimaryKey(Author::getId);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());

		schema.addEntityDescriptor(Genre.class)
			.setPrimaryKey(Genre::getId);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}


}
