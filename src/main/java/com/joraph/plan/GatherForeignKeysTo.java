package com.joraph.plan;

/**
 * Represents a step in a query plan where the foreign key values will need
 * to be retrieved for a subsequent step.
 */
public class GatherForeignKeysTo
	extends AbstractOperation
	implements Operation {

	private final Class<?> entityClass;

	/**
	 * Creates a new instance of GatherForeignKeysTo.
	 * @param entityClass the entity class
	 */
	public GatherForeignKeysTo(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	@Override
	public String explain() {
		return new StringBuilder()
			.append(" - ")
			.append(getClass().getSimpleName())
			.append(" gather fks to ").append(entityClass.getName())
			.toString();
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
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
		GatherForeignKeysTo other = (GatherForeignKeysTo) obj;
		if (entityClass == null) {
			if (other.entityClass != null)
				return false;
		} else if (!entityClass.equals(other.entityClass))
			return false;
		return true;
	}

}
