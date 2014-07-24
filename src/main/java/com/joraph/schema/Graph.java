package com.joraph.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class Graph<T>
	implements Cloneable {

	private static final int UNMARKED = 0, TEMP = 1, MARKED = 2;

	private Comparator<T> comparator;
	private Set<T> entities;
	private Map<T, Set<T>> outgoingEdge;
	private Map<T, Set<T>> incomingEdge;
	private Map<T, Integer> marked;

	private Stack<Graph<T>> snapshots = new Stack<>();

	public Graph(Comparator<T> comparator) {
		this.comparator = comparator;
		entities 		= new TreeSet<>(comparator);
		outgoingEdge 	= new HashMap<>();
		incomingEdge 	= new HashMap<>();
		marked			= new HashMap<>();
	}

	public List<T> topSort() {

		// get entities with no incoming FKs
		snapshot();
		Stack<T> stack = new Stack<>();
		stack.addAll(getEntitiesWithoutIncomingEdges());

		// sort
		List<T> order = new ArrayList<>();
		while (!stack.isEmpty()) {
			T n = stack.pop();
			order.add(n);
			
			for (T m : getEntities()) {
				if (!hasOutgoingEdge(n, m)) {
					continue;
				}
				removeEdge(n, m);
				if (!hasIncomingEdge(m)) {
					stack.push(m);
				}
			}
		}
		rollback();

		// return it
		return order;
	}

	public List<T> depthFirstSort() {
		LinkedList<T> ret = new LinkedList<>();
		markAll(UNMARKED);
		T node = null;
		while ((node=getMarked(UNMARKED))!=null) {
			dfsVisit(node, ret);
		}
		unmarkAll();
		return ret;
	}

	private void dfsVisit(T node, LinkedList<T> ret) {
		if (getMark(node)==TEMP) {
			throw new IllegalStateException("Not a DAG, can't DFS");
		}
		mark(node, TEMP);
		for (T m : entities) {
			if (hasOutgoingEdge(node, m)) {
				dfsVisit(m, ret);
			}
		}
		mark(node, MARKED);
		ret.addFirst(node);
	}

	public void mark(T node, Integer mark) {
		marked.put(node, mark);
	}

	public Integer getMark(T node) {
		return marked.get(node);
	}

	public Integer unmark(T node) {
		return marked.remove(node);
	}

	public void unmarkAll() {
		marked.clear();
	}

	public void markAll(Integer mark) {
		for (T node : entities) {
			mark(node, mark);
		}
	}

	public T getMarked(Integer mark) {
		for (Entry<T, Integer> entry : marked.entrySet()) {
			if (entry.getValue()==mark) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void snapshot() {
		try {
			snapshots.push(clone());
		} catch (CloneNotSupportedException e) {
			// swallow
		}
	}

	public void rollback() {
		if (snapshots.isEmpty()) {
			throw new IllegalStateException();
		}
		Graph<T> snapshot = snapshots.pop();
		this.entities = snapshot.entities;
		this.outgoingEdge = snapshot.outgoingEdge;
		this.incomingEdge = snapshot.incomingEdge;
		this.marked = snapshot.marked;
	}

	public void reverse() {
		Map<T, Set<T>> temp = incomingEdge;
		incomingEdge = outgoingEdge;
		outgoingEdge = temp;
	}

	@Override
	public Graph<T> clone()
		throws CloneNotSupportedException {
		Graph<T> ret = new Graph<T>(comparator);
		ret.entities.addAll(entities);
		for (Entry<T, Set<T>> entry : outgoingEdge.entrySet()) {
			ret.outgoingEdge.put(entry.getKey(), new HashSet<T>());
			ret.outgoingEdge.get(entry.getKey()).addAll(entry.getValue());
		}
		for (Entry<T, Set<T>> entry : incomingEdge.entrySet()) {
			ret.incomingEdge.put(entry.getKey(), new HashSet<T>());
			ret.incomingEdge.get(entry.getKey()).addAll(entry.getValue());
		}
		ret.snapshots.addAll(snapshots);
		ret.marked.putAll(marked);
		return ret;
	}

	public Set<T> getEntitiesWithoutIncomingEdges() {
		Set<T> ret = new HashSet<>();
		for (T entity : entities) {
			if (countEdges(entity, true)==0) {
				ret.add(entity);
			}
		}
		return ret;
	}

	public Set<T> getEntities() {
		return Collections.unmodifiableSet(entities);
	}

	public void addEntity(T entity) {
		entities.add(entity);
	}

	public void removeEntity(T entity) {
		entities.remove(entity);
		outgoingEdge.remove(entity);
		incomingEdge.remove(entity);
		for (T key : incomingEdge.keySet()) {
			incomingEdge.get(key).remove(entity);
		}
		for (T key : outgoingEdge.keySet()) {
			outgoingEdge.get(key).remove(entity);
		}
	}

	public void addEdge(T from, T to) {
		addEntity(from);
		addEntity(to);
		if (!outgoingEdge.containsKey(from)) {
			outgoingEdge.put(from, new HashSet<T>());
		}
		if (!incomingEdge.containsKey(to)) {
			incomingEdge.put(to, new HashSet<T>());
		}
		outgoingEdge.get(from).add(to);
		incomingEdge.get(to).add(from);
	}

	public void removeEdge(T from, T to) {
		if (outgoingEdge.containsKey(from)) {
			outgoingEdge.get(from).remove(to);
		}
		if (incomingEdge.containsKey(to)) {
			incomingEdge.get(to).remove(from);
		}
	}

	public boolean hasOutgoingEdge(T from, T to) {
		return outgoingEdge.containsKey(from)
			&& outgoingEdge.get(from).contains(to);
	}

	public boolean hasIncomingEdge(T to) {
		return incomingEdge.containsKey(to)
			&& !incomingEdge.get(to).isEmpty();
	}

	public List<T> sortByEdgeCount(final boolean incoming) {
		List<T> sort = new ArrayList<>();
		sort.addAll(entities);
		Collections.sort(sort, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return new Integer(countEdges(o1, incoming))
					.compareTo(new Integer(countEdges(o2, incoming)));
			}
		});
		return sort;
	}

	public int countEdges(T node, boolean incoming) {
		Set<T> edges = (incoming) ? incomingEdge.get(node) : outgoingEdge.get(node);
		return (edges!=null) ? edges.size() : 0;
	}

}
