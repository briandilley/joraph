package com.joraph.schema

object SchemaUtil {

    /**
     * Determines whether or not the foreign entity defined by the given [ForeignKey] should be
     * loaded based on the given arguments. This always returns true for plain [ForeignKey] types,
     * but may return false for [ConditionalForeignKey] types.
     */
    @JvmStatic
    fun shouldLoad(fk: ForeignKey<*, *>, arguments: List<Any?>?)
            = fk !is ConditionalForeignKey<*, *, *> || fk.shouldLoad(arguments)

    /**
     * Creates and returns a [Key] composed of the values returned by the given functions after
     * having been passed through the given converter function.
     */
    @JvmStatic
    fun <T, R> compositeKey(converter: (Array<Any?>) -> R?, first: (T) -> Any?, vararg remaining: (T) -> Any?): Key<T, R> {
        return Key {
            arrayOf(first, *remaining)
                    .map { a -> it?.let(a::invoke) }
                    .toTypedArray()
                    .let(converter)
        }
    }

}
