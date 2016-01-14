package com.joraph.schema;

/**
 * An exception which occurs when the foreign key is unknown.
 */
@SuppressWarnings("serial")
public class UnknownGraphFKException
	extends UnknownFKException {

	/**
	 * @param entityClass the entity class
	 * @param fk the foreign key that is unknown
	 */
	public UnknownGraphFKException(Class<?> entityClass, ForeignKey<?, ?> fk) {
		this(entityClass, fk, "Unknown Graph FK: "+entityClass.getName()+"."+fk+" -> "+fk.getForeignEntity().getName());
	}

	/**
	 * @param entityClass the entity class
	 * @param fk the foreign key that is unknown
	 */
	public UnknownGraphFKException(Class<?> entityClass, ForeignKey<?, ?> fk, String message) {
		super(entityClass, fk, message);
	}

}
