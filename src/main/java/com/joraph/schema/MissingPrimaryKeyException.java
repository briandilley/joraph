package com.joraph.schema;

import com.joraph.JoraphException;

/**
 * An exception which occurs when the primary key is absent.
 */
@SuppressWarnings("serial")
public class MissingPrimaryKeyException
	extends JoraphException {

	private final EntityDescriptor entityDescriptor;

	/**
	 * Creates a new instance of MissingPrimaryKeyException
	 * @param entityDescriptor the entity descriptor for which the primary key is absent
	 */
	public MissingPrimaryKeyException(EntityDescriptor entityDescriptor) {
		super(entityDescriptor.getEntityClass().getName()+" is missing a primary key");
		this.entityDescriptor = entityDescriptor;
	}

	/**
	 * @return the entityDescriptor
	 */
	public EntityDescriptor getEntityDescriptor() {
		return entityDescriptor;
	}

}
