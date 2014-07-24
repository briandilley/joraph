package com.joraph.schema;

import java.beans.IntrospectionException;
import java.io.Serializable;

import com.joraph.util.ReflectionUtil;

public class ForeignKey<T extends Serializable>
	extends AbstractProperty<T>
	implements Property<T> {

	private Class<?> entityClass;
	private Class<?> foreignEntity;

	public ForeignKey(String propertyName, Class<?> entityClass, Class<?> foreignEntity)
		throws IntrospectionException {
		this.entityClass = entityClass;
		this.foreignEntity = foreignEntity;
		super.setDescriptor(ReflectionUtil.getPropertyDescriptor(entityClass, propertyName));
	}

	/**
	 * @return the foreignEntity
	 */
	public Class<?> getForeignEntity() {
		return foreignEntity;
	}

	@Override
	public String toString() {
		return entityClass.getName()
			+"."+getName()+"->"
			+foreignEntity.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityClass == null) ? 0 : entityClass.hashCode());
		result = prime * result
				+ ((foreignEntity == null) ? 0 : foreignEntity.hashCode());
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
		ForeignKey<?> other = (ForeignKey<?>)obj;
		if (entityClass == null) {
			if (other.entityClass != null)
				return false;
		} else if (!entityClass.equals(other.entityClass))
			return false;
		if (foreignEntity == null) {
			if (other.foreignEntity != null)
				return false;
		} else if (!foreignEntity.equals(other.foreignEntity))
			return false;
		return true;
	}

}
