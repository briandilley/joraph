package com.joraph.plan;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.joraph.Context;
import com.joraph.schema.Author;
import com.joraph.schema.Book;
import com.joraph.schema.Checkout;
import com.joraph.schema.EntityDescriptor;
import com.joraph.schema.Genre;
import com.joraph.schema.Library;
import com.joraph.schema.Schema;
import com.joraph.schema.SimilarBook;
import com.joraph.schema.User;

public class ExecutionPlannerTest {

	private Schema schema;
	private Context context;
	private ExecutionPlanner planner;

	@Before
	public void setUp()
		throws Exception {
		schema = new Schema();
		context = new Context(schema);
		planner = new ExecutionPlanner(context);

		EntityDescriptor author = schema.addEntityDescriptor(Author.class);
		author.setPrimaryKey("id");
		
		EntityDescriptor genre = schema.addEntityDescriptor(Genre.class);
		genre.setPrimaryKey("id");
		
		EntityDescriptor user = schema.addEntityDescriptor(User.class);
		user.setPrimaryKey("id");
		
		EntityDescriptor library = schema.addEntityDescriptor(Library.class);
		library.setPrimaryKey("id");
		library.addForeignKey("librarianUserId", User.class);
		
		EntityDescriptor book = schema.addEntityDescriptor(Book.class);
		book.setPrimaryKey("id");
		book.addForeignKey("authorId", Author.class);
		book.addForeignKey("genreId", Genre.class);
		book.addForeignKey("libraryId", Library.class);
		
		EntityDescriptor checkout = schema.addEntityDescriptor(Checkout.class);
		checkout.setPrimaryKey("id");
		checkout.addForeignKey("bookId", Book.class);
		checkout.addForeignKey("userId", User.class);
		checkout.addForeignKey("libraryId", Library.class);
		
		EntityDescriptor similarBook = schema.addEntityDescriptor(SimilarBook.class);
		similarBook.setPrimaryKey("id");
		similarBook.addForeignKey("bookId", Book.class);
		similarBook.addForeignKey("similarBookId", Book.class);
		
		schema.validate();
	}

	@After
	public void tearDown()
		throws Exception {
		schema = null;
		context = null;
		planner = null;
	}

	@Test
	public void testPlan() {
		ExecutionPlan plan = planner.plan(Checkout.class);
		assertNotNull(plan);
		System.out.println(plan.explain());
	}

}
