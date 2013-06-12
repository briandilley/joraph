package com.joraph.plan;

import static org.junit.Assert.*;

import java.util.List;

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
		checkout.addForeignKey("userId", User.class);
		checkout.addForeignKey("libraryId", Library.class);
		checkout.addForeignKey("bookId", Book.class);
		
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
	public void testPlan_Checkout() {
		// check plan
		ExecutionPlan plan = planner.plan(Checkout.class);
		assertNotNull(plan);
		System.out.println(plan.explain());

		// check ops
		List<Operation> ops = plan.getOperations();
		
		// get book fks and load them
		assertTrue(ops.get(0).equals(new GatherForeignKeysTo(Book.class)));
		assertTrue(ops.get(1).equals(new LoadOperation(Book.class)));
		
		// get libray fks and load them
		assertTrue(ops.get(2).equals(new GatherForeignKeysTo(Library.class)));
		assertTrue(ops.get(3).equals(new LoadOperation(Library.class)));
		
		// get the rest of the fks and load
		assertTrue(ops.subList(4, 7).contains(new GatherForeignKeysTo(Author.class)));
		assertTrue(ops.subList(4, 7).contains(new GatherForeignKeysTo(Genre.class)));
		assertTrue(ops.subList(4, 7).contains(new GatherForeignKeysTo(User.class)));
		assertTrue(ops.subList(7, 10).contains(new LoadOperation(Author.class)));
		assertTrue(ops.subList(7, 10).contains(new LoadOperation(Genre.class)));
		assertTrue(ops.subList(7, 10).contains(new LoadOperation(User.class)));

	}

	@Test
	public void testPlan_SimilarBook() {
		// check plan
		ExecutionPlan plan = planner.plan(SimilarBook.class);
		assertNotNull(plan);
		System.out.println(plan.explain());


	}

}
