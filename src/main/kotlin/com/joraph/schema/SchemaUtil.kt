package com.joraph.schema

object SchemaUtil {

    @JvmStatic
    fun shouldLoad(fk: ForeignKey<*, *>, arguments: List<Any?>?)
            = fk !is ConditionalForeignKey<*, *, *> || fk.shouldLoad(arguments)

    @JvmStatic
    fun <T, R> compositeKey(converter: Function1<Array<Any?>, R?>, first: Function1<T, Any?>, vararg remaining: Function1<T, Any?>): Key<T, R> {
        return Key {
            arrayOf(first, *remaining)
                    .map { a -> it?.let(a::invoke) }
                    .toTypedArray()
                    .let(converter)
        }
    }

}
