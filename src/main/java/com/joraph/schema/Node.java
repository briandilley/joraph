package com.joraph.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Node {

	private final Schema schema;
	private final Node parent;
	private final Class<?> entityClass;
	private final Collection<ForeignKey<?, ?>> foreignKeys;
	private final boolean circular;

	/**
	 * For visiting via {@link #visit(Node)}.
	 */
	public interface Visitor {
		public enum Result {
			RETURN_THIS,
			SKIP_SIBLINGS,
			SKIP_CHILDREN,
			CONTINUE
		}
		Result visit(Node node);
	}

	public Node(Node parent, Schema schema, Class<?> entityClass, Collection<ForeignKey<?, ?>> foreignKeys, boolean circular) {
		this.parent 		= parent;
		this.schema			= schema;
		this.entityClass 	= entityClass;
		this.foreignKeys 	= foreignKeys;
		this.circular 		= circular;
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * @return the foreignKeys
	 */
	public Collection<ForeignKey<?, ?>> getForeignKeys() {
		return Collections.unmodifiableCollection(foreignKeys);
	}

	/**
	 * @return the circular
	 */
	public boolean isCircular() {
		return circular;
	}

	/**
	 * @return the parent
	 */
	public Node parent() {
		return parent;
	}

	/**
	 * Visits the children of this node.
	 * @param visitor the visitor
	 * @return the node
	 */
	public Node visitChildren(Visitor visitor) {
		return visit(this.getChildren(), visitor);
	}

	/**
	 * Does the work of visiting.
	 * @param nodes
	 * @param visitor
	 * @return
	 */
	private Node visit(Collection<Node> nodes, Visitor visitor) {
		for (Node node : nodes) {
			switch (visitor.visit(node)) {
				case RETURN_THIS:
					return node;
				case SKIP_CHILDREN:
					continue;
				case SKIP_SIBLINGS:
					return null;
				case CONTINUE:
					Node n = visit(node.getChildren(), visitor);
					if (n!=null) {
						return n;
					}
			}
		}
		return null;
	}

	/**
	 * Returns the children of this node.
	 * @return the children
	 */
	public Set<Node> getChildren() {
		Set<Node> ret = new HashSet<>();
		for (ForeignKey<?, ?> fk : getForeignKeys()) {
			ret.add(child(fk));
		}
		return ret;
	}

	/**
	 * Returns the children at the bottom of this graph
	 * begining at this node.
	 * @return the children
	 */
	public Set<Node> getBottomChildren() {
		final Set<Node> ret = new HashSet<>();
		this.visitChildren(new Visitor(){
			@Override
			public Result visit(Node node) {
				if (!node.circular) {
					return Result.CONTINUE;
				} else if (node.parent!=null) {
					ret.add(node.parent);
				}
				return Result.SKIP_CHILDREN;
			}
		});
		ret.remove(this);
		return ret;
	}

	/**
	 * Returns the child for the given property name.
	 * @return the child
	 */
	public Node child(ForeignKey<?, ?> prop) {
		for (ForeignKey<?, ?> fk : foreignKeys) {
			if (fk.equals(prop)) {
				Class<?> foreignClass = fk.getForeignEntity();
				return new Node(
					this, schema,
					foreignClass,
					schema.describeForeignKeysFrom(foreignClass),
					this.entityClass.equals(foreignClass) || getAncestor(foreignClass)!=null);
			}
		}
		return null;
	}

	/**
	 * Returns the first child that equals {@code entityClass}.
	 * @param entityClass the entity class
	 * @param traverse whether or not to traverse
	 * @return
	 */
	public Node getChild(final Class<?> entityClass, final boolean traverse) {
		return this.visitChildren(new Visitor() {
			@Override
			public Result visit(Node node) {
				if (node.entityClass.equals(entityClass)) {
					return Result.RETURN_THIS;
				}
				return traverse ? Result.CONTINUE : Result.SKIP_CHILDREN;
			}
		});
	}

	/**
	 * Returns the first child assignable by {@code entityClass}.
	 * @param entityClass the entity class
	 * @param traverse whether or not to traverse
	 * @return
	 */
	public Node getChildAssignable(final Class<?> entityClass, final boolean traverse) {
		return this.visitChildren(new Visitor() {
			@Override
			public Result visit(Node node) {
				if (entityClass.isAssignableFrom(node.entityClass)) {
					return Result.RETURN_THIS;
				}
				return traverse ? Result.CONTINUE : Result.SKIP_CHILDREN;
			}
		});
	}

	/**
	 * Indicates whether or not this node has the given ancestor.
	 * @param entityClass the entity
	 * @return true or false
	 */
	public Node getAncestor(Class<?> entityClass) {
		for (Node n=this.parent; n!=null; n=n.parent) {
			if (n.entityClass.equals(entityClass)) {
				return n;
			}
		}
		return null;
	}

	/**
	 * Indicates whether or not this node has the given ancestor.
	 * @param entityClass the entity
	 * @return true or false
	 */
	public Node getAncestorAssignable(Class<?> entityClass) {
		for (Node n=this.parent; n!=null; n=n.parent) {
			if (entityClass.isAssignableFrom(n.entityClass)) {
				return n;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityClass == null) ? 0 : entityClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (entityClass == null) {
			if (other.entityClass != null)
				return false;
		} else if (!entityClass.equals(other.entityClass))
			return false;
		return true;
	}

}
