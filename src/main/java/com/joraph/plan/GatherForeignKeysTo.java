package com.joraph.plan;

public class GatherForeignKeysTo
	implements Operation {

	private Class<?> entityClass;

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

}
