package com.joraph.plan;

public class LoadOperation
	implements Operation {

	private Class<?> entityClass;

	public LoadOperation(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	@Override
	public String explain() {
		StringBuilder ret = new StringBuilder()
			.append(" - ")
			.append(getClass().getSimpleName())
			.append(" load ").append(entityClass.getName());
		return ret.toString();
	}

}
