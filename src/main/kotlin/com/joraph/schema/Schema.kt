package com.joraph.schema

class Schema {

    var isValidated = false
        private set

    private val entityDescriptors: MutableMap<Class<*>, EntityDescriptor<*>> = mutableMapOf()

    /**
     * Asserts that the schema has been validated.
     */
    fun assertValidated() {
        check(isValidated) { "Schema not validated" }
    }

    /**
     * Returns the graph type key for the given entity class.
     * @param entityClass the entity class
     * @return the key
     */
    fun getGraphTypeKey(entityClass: Class<*>): Class<*> {
        val desc = entityDescriptors[entityClass]
            ?: return entityClass
        return desc.graphKey
    }

    /**
     * @param entityClass the entity class
     * @return the descriptor for the class, or null if no descriptor exists for it
     */
    fun getEntityDescriptors(entityClass: Class<*>): Set<EntityDescriptor<*>> {
        return entityDescriptors.values
            .filter { it.entityClass == entityClass || it.graphKey == entityClass }
            .toSet()
    }

    /**
     * Adds an entity descriptor to the schema.
     * @param entityDescriptor the entity descriptor to add
     */
    fun addEntityDescriptor(entityDescriptor: EntityDescriptor<*>) {
        isValidated = false
        entityDescriptors[entityDescriptor.entityClass] = entityDescriptor
    }

    /**
     * Wraps a class in an [com.joraph.schema.EntityDescriptor] and then
     * adds it to the schema.
     * @param entityClass the entity class
     * @return the [com.joraph.schema.EntityDescriptor] created as a wrapper and
     * added to the schema
     */
    fun <T> addEntityDescriptor(entityClass: Class<T>): EntityDescriptor<T> {
        isValidated = false
        val ret = EntityDescriptor(entityClass)
        addEntityDescriptor(ret)
        return ret
    }

    /**
     * Describes the foreign keys from one class to another, if any exist.
     * @param fromEntityClass the from entity class
     * @param toEntityClass the to entity class
     * @return all of the foreign keys which exist from one entity class to
     * another or an empty collection if no such relationships exist
     */
    fun describeForeignKeys(fromEntityClass: Class<*>, toEntityClass: Class<*>): List<ForeignKey<*, *>> {
        return describeForeignKeysFrom(fromEntityClass)
            .filter { it.foreignEntity == toEntityClass }
            .toList()
    }

    /**
     * Describes the foreign keys configured for a specified class.
     * @param entityClass the entity class
     * @return the foreign keys configured for that class
     */
    fun describeForeignKeysFrom(entityClass: Class<*>): List<ForeignKey<*, *>> {
        return getEntityDescriptors(entityClass)
            .flatMap { it.foreignKeys }
    }

    /**
     * Describes the foreign keys coming into a given entity class.
     * @param toEntityClass the entity class
     * @return foreign keys that point to an entity class
     */
    fun describeForeignKeysTo(toEntityClass: Class<*>): List<ForeignKey<*, *>> {
        return entityDescriptors.values
            .flatMap { it.foreignKeys }
            .filter { it.foreignEntity == toEntityClass }
    }

    /**
     * Describes all of the foreign keys that have been configured.
     * @return all of the configured foreign keys
     */
    fun describeForeignKeys(): List<ForeignKey<*, *>> {
        return entityDescriptors.values
            .flatMap { it.foreignKeys }
    }

    /**
     * Describes the given entity class.
     */
    fun describeEntities(): Set<Class<*>> {
        assertValidated()
        return entityDescriptors.keys
    }

    /**
     * Validates the [Schema].
     */
    fun validate(): Schema {

        // check each entity descriptor
        for ((_, ed) in entityDescriptors) {

            // check pk
            ed.primaryKey

            // check FKs
            val fks: Set<ForeignKey<*, *>> = ed.foreignKeys
            for (fk in fks) {
                entityDescriptors.values
                    .find {  it.entityClass == fk.foreignEntity || it.graphKey == fk.foreignEntity }
                    ?: throw UnknownFKException(ed.entityClass, fk)
            }
        }

        // good to go
        isValidated = true
        return this
    }

}
