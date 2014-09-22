package com.joraph.schema;

import com.joraph.JoraphException;

/**
 * An exception which occurs when the foreign key is unknown.
 */
@SuppressWarnings("serial")
public class UnknownFKException
	extends JoraphException {

	private final Class<?> entityClass;
	private final ForeignKey<?> fk;

	/**
	 * Creates a new instance of UnknownFKException.
	 * @param entityClass the entity class
	 * @param fk the foreign key that is unknown
	 */
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
