package com.joraph.debug;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoaderDebug {

	private Class<?> entityClass;
	private Set<Object> entityIds;
	private List<?> loadedEntities;
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

	public Set<Object> getEntityIds() {
		return entityIds;
	}

	public void setEntityIds(Set<Object> ids) {
		this.entityIds = new HashSet<Object>(ids);
	}

	public List<?> getLoadedEntities() {
		return loadedEntities;
	}

	public void setLoadedEntities(List<?> objects) {
		this.loadedEntities = objects;
	}

}
