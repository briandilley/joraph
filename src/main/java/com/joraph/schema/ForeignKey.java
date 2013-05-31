package com.joraph.schema;

import java.beans.IntrospectionException;
import java.io.Serializable;

import com.joraph.util.ReflectionUtil;

public class ForeignKey<T extends Serializable>
	extends AbstractProperty<T>
	implements Property<T> {

	private Class<?> foreignEntity;
	private boolean eagar = true;

	public ForeignKey(String propertyName, Class<?> entityClass, Class<?> foreignEntity, boolean eagar)
		throws IntrospectionException {
		this.foreignEntity = foreignEntity;
		this.eagar = eagar;
		super.setDescriptor(ReflectionUtil.getPropertyDescriptor(entityClass, propertyName));
	}

	/**
	 * @return the foreignEntity
	 */
	public Class<?> getForeignEntity() {
		return foreignEntity;
	}

	/**
	 * @return the eagar
	 */
	public boolean isEagar() {
		return eagar;
	}

}
