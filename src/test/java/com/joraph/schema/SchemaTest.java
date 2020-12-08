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
	public void testCircular() {
		schema = new Schema();
		schema.addEntityDescriptor(Author.class)
				.withPrimaryKey(Author::getId)
				.withForeignKey(Author.class, Author::getId);
		schema.validate();

		schema = new Schema();
		schema.addEntityDescriptor(Author.class)
				.withPrimaryKey(Author::getId)
				.withForeignKey(User.class, Author::getId);
		schema.addEntityDescriptor(User.class)
				.withPrimaryKey(User::getId)
				.withForeignKey(Library.class, User::getId);
		schema.addEntityDescriptor(Library.class)
				.withPrimaryKey(Library::getId)
				.withForeignKey(Author.class, Library::getId);
		schema.validate();
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
					.withPrimaryKey(Author::getId)
					.withForeignKey(Book.class, Author::getId);
			schema.validate();
		});
	}

	@Test
	public void testValidateSingleEntity()
		throws Exception {
		schema.addEntityDescriptor(Author.class)
				.withPrimaryKey(Author::getId);
		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}

	@Test
	public void testLargeSchema()
		throws Exception {
		schema.addEntityDescriptor(Author.class)
			.withPrimaryKey(Author::getId);

		schema.addEntityDescriptor(Genre.class)
			.withPrimaryKey(Genre::getId);

		schema.addEntityDescriptor(Book.class)
			.withPrimaryKey(Book::getId)
			.withForeignKey(Author.class, Book::getAuthorId)
			.withForeignKey(Genre.class, Book::getGenreId);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}

	@Test
	public void testDirty()
		throws Exception {
		schema.addEntityDescriptor(Author.class)
			.withPrimaryKey(Author::getId);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());

		schema.addEntityDescriptor(Genre.class)
			.withPrimaryKey(Genre::getId);

		assertFalse(schema.isValidated());
		schema.validate();
		assertTrue(schema.isValidated());
	}


}
