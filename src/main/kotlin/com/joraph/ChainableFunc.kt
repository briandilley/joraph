package com.joraph

data class ChainableFunc<T, R>(private val func: Function1<T, R?>) : Function1<T, R?> {

    companion object {
        @JvmStatic
        fun <T, R> chain(func: Function1<T, R?>): ChainableFunc<T, R?>
                = ChainableFunc(func)
    }

    override fun invoke(p1: T): R? = func.invoke(p1)

    fun asJavaFunction(): java.util.function.Function<T, R>
        = java.util.function.Function<T, R> { func.invoke(it) }

    fun read(p1: T): R? = invoke(p1)

    fun get(p1: T): R? = invoke(p1)

    infix fun <N> andThen(next: Function1<R, N?>): ChainableFunc<T, N>
            = ChainableFunc(func andThen next)

}

fun <T, R> Function1<T, R?>.chain(): ChainableFunc<T, R> = ChainableFunc(this)
