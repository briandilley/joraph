package com.joraph.plan;

public class GatherForeignKeysFrom
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

}
