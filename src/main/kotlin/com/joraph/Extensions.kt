package com.joraph

import com.joraph.schema.EntityDescriptor
import java.util.function.Function

@Suppress("UNCHECKED_CAST")
fun <T> Collection<EntityDescriptor<*>>.findFirstByEntityClass(clazz: Class<*>): EntityDescriptor<T>?
        = this.find { it.entityClass == clazz } as EntityDescriptor<T>?

@Suppress("UNCHECKED_CAST")
fun <T> Collection<EntityDescriptor<*>>.findFirstByGraphKey(clazz: Class<*>): EntityDescriptor<T>?
        = this.find { it.graphKey == clazz } as EntityDescriptor<T>?

infix fun <T, R, N> Function1<T, R?>.andThen(next: Function1<R, N?>): Function1<T, N?> {
    return { o -> this.invoke(o)?.let(next) }
}

infix fun <T, R, N> Function1<T, R?>.andThen(next: Function<R, N?>): Function1<T, N?> {
    return { o -> this.invoke(o)?.let { next.apply(it) } }
}

infix fun <T, R, N> Function<T, R?>.andThen(next: Function<R, N?>): Function1<T, N?> {
    return { o -> this.apply(o)?.let { next.apply(it) } }
}
