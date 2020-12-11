package com.joraph

object CollectionUtil {

    @JvmStatic
    fun <T> toList(itr: Iterable<T>): MutableList<T> {
        if (itr is MutableList<*>) {
            return itr as MutableList<T>
        }
        val ret: MutableList<T> = mutableListOf()
        itr.forEach(ret::add)
        return ret
    }

    @JvmStatic
    fun <T> toSet(itr: Iterable<T>): MutableSet<T> {
        if (itr is MutableSet<*>) {
            return itr as MutableSet<T>
        }
        val ret: MutableSet<T> = mutableSetOf()
        itr.forEach(ret::add)
        return ret
    }

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

