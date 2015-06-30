package com.joraph.debug;

public class LoaderDebug {

	private Class<?> entityClass;
	private Long loaderTimeMillis;
	private Integer entityIdCount;
	private Integer loadedEntityCount;

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public Long getLoaderTimeMillis() {
		return loaderTimeMillis;
	}

	public void setLoaderTimeMillis(Long loaderTimeMillis) {
		this.loaderTimeMillis = loaderTimeMillis;
	}

	public Integer getEntityIdCount() {
		return entityIdCount;
	}

	public void setEntityIdCount(Integer entityIdCount) {
		this.entityIdCount = entityIdCount;
	}

	public Integer getLoadedEntityCount() {
		return loadedEntityCount;
	}

	public void setLoadedEntityCount(Integer loadedEntityCount) {
		this.loadedEntityCount = loadedEntityCount;
	}

}
