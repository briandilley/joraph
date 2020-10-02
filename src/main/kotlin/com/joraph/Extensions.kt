package com.joraph

import com.joraph.schema.EntityDescriptor

@Suppress("UNCHECKED_CAST")
fun <T> Collection<EntityDescriptor<*>>.findFirstByEntityClass(clazz: Class<*>): EntityDescriptor<T>?
        = this.find { it.entityClass == clazz } as EntityDescriptor<T>?

@Suppress("UNCHECKED_CAST")
fun <T> Collection<EntityDescriptor<*>>.findFirstByGraphKey(clazz: Class<*>): EntityDescriptor<T>?
        = this.find { it.graphKey == clazz } as EntityDescriptor<T>?
