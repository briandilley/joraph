package com.joraph.schema

/**
 * Can be used as a value for a [Key] or other [Property] when the value
 * needs to be made up of multiple other values.
 */
class CompositeValue {

    companion object {

        @JvmField
        val CONVERTER: ((Array<Any?>) -> Any) = ::CompositeValue
    }

    val parts: Array<Any?>
    val size: Int get() = parts.size

    constructor(vararg objects: Any?) {
        parts = arrayOf(*objects)
    }

    constructor(size: Int) {
        parts = arrayOfNulls(size)
    }

    operator fun get(index: Int): Any? = parts[index]

    operator fun set(index: Int, value: Any?) {
        parts[index] = value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CompositeValue
        if (!parts.contentEquals(other.parts)) return false
        return true
    }

    override fun hashCode(): Int {
        return parts.contentHashCode()
    }

}
