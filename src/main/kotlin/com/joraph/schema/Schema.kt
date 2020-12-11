package com.joraph.schema

/**
 * The Schema holds all of the meta-data about an object graph, that is all of it's entities and their
 * primary and foreign keys.
 */
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
     * Returns the graph type key for the given entity class. This is the key that
     * will be used to store the given entity in the [com.joraph.ObjectGraph]. This is
     * useful for polymorphic types that need to be addressed as the same super type.
     */
    fun getGraphTypeKey(entityClass: Class<*>): Class<*> {
        val desc = entityDescriptors[entityClass]
            ?: return entityClass
        return desc.graphKey
    }

    /**
     * Returns all of the [EntityDescriptor] configured for the given entity.
     */
    fun getEntityDescriptors(entityClass: Class<*>): Set<EntityDescriptor<*>> {
        return entityDescriptors.values
            .filter { it.entityClass == entityClass || it.graphKey == entityClass }
            .toSet()
    }

    /**
     * Adds an [EntityDescriptor] to the schema.
     */
    fun addEntityDescriptor(entityDescriptor: EntityDescriptor<*>) {
        isValidated = false
        entityDescriptors[entityDescriptor.entityClass] = entityDescriptor
    }

    /**
     * Adds to the schema and returns an empty [EntityDescriptor] for the given entity.
     */
    fun <T> addEntityDescriptor(entityClass: Class<T>): EntityDescriptor<T> {
        isValidated = false
        val ret = EntityDescriptor(entityClass)
        addEntityDescriptor(ret)
        return ret
    }

    /**
     * Returns a [List] of [ForeignKey]s defined from the [fromEntityClass] to the [toEntityClass].
     */
    fun describeForeignKeys(fromEntityClass: Class<*>, toEntityClass: Class<*>): List<ForeignKey<*, *>> {
        return describeForeignKeysFrom(fromEntityClass)
            .filter { it.foreignEntity == toEntityClass }
            .toList()
    }

    /**
     * Returns a [List] of [ForeignKey]s defined on the [entityClass].
     */
    fun describeForeignKeysFrom(entityClass: Class<*>): List<ForeignKey<*, *>> {
        return getEntityDescriptors(entityClass)
            .flatMap { it.foreignKeys }
    }

    /**
     * Returns a [List] of [ForeignKey]s defined on other entities to the [entityClass].
     */
    fun describeForeignKeysTo(entityClass: Class<*>): List<ForeignKey<*, *>> {
        return entityDescriptors.values
            .flatMap { it.foreignKeys }
            .filter { it.foreignEntity == entityClass }
    }

    /**
     * Returns a [List] of all [ForeignKey]s defined.
     */
    fun describeForeignKeys(): List<ForeignKey<*, *>> {
        return entityDescriptors.values
            .flatMap { it.foreignKeys }
    }

    /**
     * Returns a [List] of all entities defined.
     */
    fun describeEntities(): Set<Class<*>> {
        assertValidated()
        return entityDescriptors.keys
    }

    /**
     * Validates the [Schema], throwing a [com.joraph.JoraphException] on error.
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
