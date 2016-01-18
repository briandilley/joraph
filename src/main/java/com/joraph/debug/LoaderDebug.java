package com.joraph.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LoaderDebug {

	private Class<?> entityClass;
	private List<?> entityIds;
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

	public List<?> getEntityIds() {
		return entityIds;
	}

	public void setEntityIds(Collection<?> ids) {
		this.entityIds = new ArrayList<Object>(ids);
	}

	public List<?> getLoadedEntities() {
		return loadedEntities;
	}

	public void setLoadedEntities(List<?> objects) {
		this.loadedEntities = objects;
	}

}
