package com.joraph.schema

import java.util.*
import java.util.function.Predicate


/**
 * Metadata about an entity class.
 */
class EntityDescriptor<T> @JvmOverloads constructor(
        val entityClass: Class<T>,
        primaryKey: Key<T, *>? = null,
        var graphKey: Class<*> = entityClass) {

    val foreignKeys: MutableMap<Function1<*, *>, ForeignKey<*, *>> = mutableMapOf()

    @JvmOverloads constructor(
            entityClass: Class<T>,
            keyFunction: Function1<T, *>,
            graphKey: Class<*> = entityClass) : this(
            entityClass = entityClass,
            primaryKey = Key(keyFunction),
            graphKey = graphKey)

    private var _primaryKey: Key<T, *>? = primaryKey

    val primaryKey: Key<T, *> get() = _primaryKey
            ?: throw MissingPrimaryKeyException(this)

    fun withGraphKey(graphKey: Class<*>): EntityDescriptor<T> {
        this.graphKey = graphKey
        return this
    }

    fun withPrimaryKey(func: Function1<T, *>): EntityDescriptor<T> {
        _primaryKey = Key(func)
        return this
    }

    @SafeVarargs
    fun withPrimaryKey(
            cconverter: Function1<Array<Any?>, *>,
            first: Function1<T, *>,
            vararg accessors: Function1<T, *>): EntityDescriptor<T> {
        _primaryKey = SchemaUtil.compositeKey(cconverter, first, *accessors)
        return this
    }

    @SafeVarargs
    fun withPrimaryKey(first: Function1<T, *>, vararg remaining: Function1<T, *>): EntityDescriptor<T> {
        return withPrimaryKey(BasicCompositeKey.CONVERTER, first, *remaining)
    }

    fun addForeignKey(foreignEntity: Class<*>): ForeignKeyBuilder<*, T> {
        return ForeignKeyBuilder<Any, T>(this, foreignEntity)
    }

    fun withForeignKey(foreignEntity: Class<*>, accessor: Function1<T, *>): EntityDescriptor<T> {
        foreignKeys[accessor] = ForeignKey(entityClass, foreignEntity, accessor)
        return this
    }

    fun <A> withForeignKey(foreignEntity: Class<*>, accessor: Function1<T, *>, argumentClass: Class<A>, argumentPredicate: Predicate<A>): EntityDescriptor<T> {
        foreignKeys[accessor] = ConditionalForeignKey(entityClass, foreignEntity, accessor, argumentClass, argumentPredicate)
        return this
    }

    inner class ForeignKeyBuilder<A, TT> internal constructor(
            private val entity: EntityDescriptor<TT>,
            private val foreignEntity: Class<*>) {

        private var accessor: Function1<TT, *>? = null
        private var argumentClass: Class<A>? = null
        private var argumentPredicate: Predicate<A>? = null

        fun withAccessor(accessor: Function1<TT, *>?): ForeignKeyBuilder<A, TT> {
            this.accessor = accessor
            return this
        }

        @Suppress("UNCHECKED_CAST")
        fun <A2> withPredicate(argumentClass: Class<A2>, argumentPredicate: Predicate<A2>): ForeignKeyBuilder<A2, TT> {
            this.argumentClass = argumentClass as Class<A>?
            this.argumentPredicate = argumentPredicate as Predicate<A>?
            return this as ForeignKeyBuilder<A2, TT>
        }

        fun add(): EntityDescriptor<TT> {
            val acc = accessor
                    ?: throw IllegalStateException("accessor is null")

            return if (argumentClass == null || argumentPredicate == null) {
                entity.withForeignKey(foreignEntity, acc)
            } else {
                entity.withForeignKey(foreignEntity, acc, argumentClass!!, argumentPredicate!!)
            }
        }
    }

    /**
     * Creates a new instance of EntityDescriptor.
     * @param entityClass the entity class
     */
    init {
        graphKey = entityClass
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntityDescriptor<*>

        if (entityClass != other.entityClass) return false
        if (graphKey != other.graphKey) return false
        if (foreignKeys != other.foreignKeys) return false
        if (_primaryKey != other._primaryKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entityClass.hashCode()
        result = 31 * result + graphKey.hashCode()
        result = 31 * result + foreignKeys.hashCode()
        result = 31 * result + (_primaryKey?.hashCode() ?: 0)
        return result
    }

}
