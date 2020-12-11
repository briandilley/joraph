package com.joraph

object CollectionUtil {

    /**
     * Returns the given [Iterable] as a [MutableList], converting it if necessary.
     */
    @JvmStatic
    fun <T> toList(itr: Iterable<T>): MutableList<T> {
        if (itr is MutableList<*>) {
            return itr as MutableList<T>
        }
        val ret: MutableList<T> = mutableListOf()
        itr.forEach(ret::add)
        return ret
    }

    /**
     * Returns the given [Iterable] as a [MutableSet], converting it if necessary.
     */
    @JvmStatic
    fun <T> toSet(itr: Iterable<T>): MutableSet<T> {
        if (itr is MutableSet<*>) {
            return itr as MutableSet<T>
        }
        val ret: MutableSet<T> = mutableSetOf()
        itr.forEach(ret::add)
        return ret
    }

    /**
     * Returns the given object as a [MutableSet], converting it if necessary, or
     * wrapping it in one if it's not already a [Collection] type.
     */
    @JvmStatic
    @Suppress("UNCHECKED")
    fun convertToSet(value: Any): MutableSet<*> {
        return when (value) {
            is MutableSet<*> -> value
            is Iterable<*> -> value.toMutableSet()
            is Array<*> -> value.toMutableSet()
            else -> mutableSetOf(value)
        }
    }
}

