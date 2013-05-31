package com.joraph.schema;

import static org.junit.Assert.*;

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
		author.setPrimaryKey("id");
		author.addForeignKey("id", Book.class);
		schema.validate();
	}

	@Test
	public void testValidateSingleEntity()
		throws Exception {
		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey("id");
		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}

	@Test
	public void testLargeSchema()
		throws Exception {
		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey("id");

		EntityDescriptor genre = schema.addEntityDescriptor(Genre.class);
		genre.setPrimaryKey("id");

		EntityDescriptor book = schema.addEntityDescriptor(Book.class);
		book.setPrimaryKey("id");
		book.addForeignKey("authorId", Author.class);
		book.addForeignKey("genreId", Genre.class);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}

	@Test
	public void testDirty()
		throws Exception {
		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey("id");

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());

		EntityDescriptor genre = schema.addEntityDescriptor(Genre.class);
		genre.setPrimaryKey("id");

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}


}
