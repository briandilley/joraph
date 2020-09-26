package com.joraph.plan;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.joraph.AbstractJoraphTest;
import com.joraph.JoraphContext;
import com.joraph.schema.Author;
import com.joraph.schema.Book;
import com.joraph.schema.Checkout;
import com.joraph.schema.Genre;
import com.joraph.schema.Library;
import com.joraph.schema.Schema;
import com.joraph.schema.User;

public class ExecutionPlannerTest
		extends AbstractJoraphTest {

	private Schema schema;
	private JoraphContext context;
	private ExecutionPlanner planner;

	@BeforeEach
	public void setUp()
			throws Exception {
		schema		= setupSchema(new Schema());
		context 	= new JoraphContext(schema);
		planner		= new ExecutionPlanner(context);
	}

	@AfterEach
	public void tearDown()
			throws Exception {
		schema = null;
		planner = null;
		context = null;
	}


	@Test
	public void testPlan_Checkout() {
		// check plan
		ExecutionPlan plan = planner.plan(Checkout.class);
		assertNotNull(plan);
		System.out.println(plan.explain());

		// check ops
		List<Operation> ops = plan.getOperations();

		assertTrue(ops.get(0).equals(new GatherForeignKeysTo(Checkout.class)));
		assertTrue(ops.get(1).equals(new LoadEntities(Checkout.class)));

		assertTrue(ops.get(2).equals(new GatherForeignKeysTo(Book.class)));
		assertTrue(ops.get(3).equals(new LoadEntities(Book.class)));

		assertTrue(ops.get(4).equals(new GatherForeignKeysTo(Library.class)));
		assertTrue(ops.get(5).equals(new LoadEntities(Library.class)));

		assertTrue(ops.get(6).equals(new GatherForeignKeysTo(Author.class)));
		assertTrue(ops.get(7).equals(new GatherForeignKeysTo(Genre.class)));
		assertTrue(ops.get(8).equals(new GatherForeignKeysTo(User.class)));

		ParallelOperation parallelOperation = new ParallelOperation();
		parallelOperation.getOperations().add(new LoadEntities(Author.class));
		parallelOperation.getOperations().add(new LoadEntities(Genre.class));
		parallelOperation.getOperations().add(new LoadEntities(User.class));
		assertTrue(ops.get(9).equals(parallelOperation));

	}

}
