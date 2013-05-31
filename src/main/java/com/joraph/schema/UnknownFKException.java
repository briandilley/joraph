package com.joraph.schema;

import com.joraph.JoraphException;

@SuppressWarnings("serial")
public class UnknownFKException
	extends JoraphException {

	private Class<?> entityClass;
	private ForeignKey<?> fk;

	public UnknownFKException(Class<?> entityClass, ForeignKey<?> fk) {
		super("Unknown FK: "+entityClass.getName()+"."+fk.getName()+" -> "+fk.getForeignEntity().getName());
		this.entityClass = entityClass;
		this.fk = fk;
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * @return the fk
	 */
	public ForeignKey<?> getFk() {
		return fk;
	}

}
