package com.joraph.plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.joraph.Context;
import com.joraph.schema.ForeignKey;
import com.joraph.schema.Node;
import com.joraph.schema.Schema;

public class ExecutionPlanner {

	private Context context = null;
	private Schema schema = null;
	private ExecutionPlan plan = null;

	/**
	 * Creates a plan for the given context.
	 * @param context the context
	 */
	public ExecutionPlanner(Context context) {
		this.context = context;
	}

	/**
	 * Clears the state.
	 */
	public void clear() {
		schema = context.getSchema();
		plan = new ExecutionPlan();
	}

	/**
	 * Creates a plan for loading an object graph
	 * starting with the given {@code entityClass}.
	 * @param ctx
	 * @param entityClass
	 * @return
	 */
	public <T> ExecutionPlan plan(Class<T> entityClass) {
		clear();
		traverse(entityClass, false);
		return plan;
	}

	/**
	 * Traverse the foreign keys.
	 * @param node starting with this node
	 */
	private void traverse(Class<?> entityClass, boolean load) {

		// describe this class
		Node node = schema.describe(entityClass);

		// get the foreign keys for this class
		final List<ForeignKey<?>> fks = new ArrayList<>();
		fks.addAll(node.getForeignKeys());

		// now do the children
		node.visitChildren(new Node.Visitor() {
			@Override
			public Result visit(Node node) {
				if (node.isCircular()) {
					return Result.SKIP_CHILDREN;
				}
				fks.addAll(node.getForeignKeys());
				return Result.CONTINUE;
			}
		});

		// count refs
		Map<Class<?>, Integer> refCounts = new HashMap<>();
		countRefs(refCounts, fks);

		// gather
		for (Entry<Class<?>, Integer> entry : refCounts.entrySet()) {
			Collection<ForeignKey<?>> fksToEntry = schema.getForeignKeys(entityClass, entry.getKey());
			if (entry.getValue()==fksToEntry.size()) {
				plan.addOperation(new GatherForeignKeysTo(entry.getKey()));
			}
		}

		// load
		for (Entry<Class<?>, Integer> entry : refCounts.entrySet()) {
			Collection<ForeignKey<?>> fksToEntry = schema.getForeignKeys(entityClass, entry.getKey());
			if (entry.getValue()==fksToEntry.size()) {
				plan.addOperation(new LoadOperation(entry.getKey()));
			}
		}

		// traverse
		List<Entry<Class<?>, Integer>> entryList = new ArrayList<>(refCounts.entrySet());
		Collections.sort(entryList, REF_COUNT_ENTRY_COMPARATOR);
		for (Entry<Class<?>, Integer> entry : entryList) {
			if (entry.getValue()>schema.getForeignKeys(entityClass, entry.getKey()).size()) {
				traverse(entry.getKey(), true);
			}
		}

		// load it
		if (load) {
			plan.addOperation(new GatherForeignKeysTo(entityClass));
			plan.addOperation(new LoadOperation(entityClass));
		}
		
	}

	/**
	 * Counts the references to each entity class in the
	 * given {@link ForeignKey}s.
	 * @param refCounts
	 * @param fks
	 */
	private void countRefs(Map<Class<?>, Integer> refCounts, Collection<ForeignKey<?>> fks) {
		for (ForeignKey<?> fk : fks) {
			if (!refCounts.containsKey(fk.getForeignEntity())) {
				refCounts.put(fk.getForeignEntity(), 0);
			}
			Integer count = refCounts.get(fk.getForeignEntity());
			count++;
			refCounts.put(fk.getForeignEntity(), count);
		}
	}

	/**
	 * For sorting FKs.
	 */
	private static final Comparator<Entry<Class<?>, Integer>> REF_COUNT_ENTRY_COMPARATOR =
		new Comparator<Entry<Class<?>, Integer>>() {
		@Override
		public int compare(Entry<Class<?>, Integer> o1, Entry<Class<?>, Integer> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	};

}
