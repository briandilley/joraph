package com.joraph.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SchemaTest {

	private Schema schema;

	@BeforeEach
	public void setUp()
		throws Exception {
		schema = new Schema();
	}

	@AfterEach
	public void tearDown()
		throws Exception {
		schema = null;
	}

	@Test
	public void testMissingPrimaryKeyException() {
		assertThrows(MissingPrimaryKeyException.class, () -> {
			schema.addEntityDescriptor(Author.class);
			schema.validate();
		});
	}

	@Test
	public void testUnknownFKException()
		throws Exception {
		assertThrows(UnknownFKException.class, () -> {
			schema.addEntityDescriptor(Author.class)
					.setPrimaryKey(Author::getId)
					.addForeignKey(Book.class, Author::getId);
			schema.validate();
		});
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
