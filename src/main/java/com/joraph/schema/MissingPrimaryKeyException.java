package com.joraph.schema;

import com.joraph.JoraphException;

@SuppressWarnings("serial")
public class MissingPrimaryKeyException
	extends JoraphException {

	private EntityDescriptor entityDescriptor;

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
