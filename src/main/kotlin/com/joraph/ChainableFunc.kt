package com.joraph

/**
 * A [ChainableFunc] makes it easy to walk through an object graph to find
 * a value.  For instance:
 * ```
 * val prop = ChainableFunc.chain(Person::address).andThen(Address::city).andThen(City::id)
 * val cityId = prop.read(person)
 * ```
 */
data class ChainableFunc<T, R>(private val func: (T) -> R?) : (T) -> R? {

    companion object {
        @JvmStatic
        fun <T, R> chain(func: (T) -> R?): ChainableFunc<T, R?>
                = ChainableFunc(func)
    }

    override fun invoke(p1: T): R? = func.invoke(p1)

    fun asJavaFunction(): java.util.function.Function<T, R>
        = java.util.function.Function<T, R> { func.invoke(it) }

    fun read(p1: T): R? = invoke(p1)

    fun get(p1: T): R? = invoke(p1)

    infix fun <N> andThen(next: (R) -> N?): ChainableFunc<T, N>
            = ChainableFunc(func andThen next)

}

fun <T, R> ((T) -> R?).chain(): ChainableFunc<T, R> = ChainableFunc(this)
