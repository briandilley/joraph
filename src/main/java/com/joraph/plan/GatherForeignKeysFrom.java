package com.joraph.plan;

public class GatherForeignKeysFrom
	extends AbstractOperation
	implements Operation {

	private Class<?> entityClass;

	public GatherForeignKeysFrom(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	@Override
	public String explain() {
		return new StringBuilder()
			.append(" - ")
			.append(getClass().getSimpleName())
			.append(" gather fks from ").append(entityClass.getName())
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
		GatherForeignKeysFrom other = (GatherForeignKeysFrom) obj;
		if (entityClass == null) {
			if (other.entityClass != null)
				return false;
		} else if (!entityClass.equals(other.entityClass))
			return false;
		return true;
	}

}
