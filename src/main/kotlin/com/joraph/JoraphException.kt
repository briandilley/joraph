package com.joraph

/**
 * Base exceptions used within the Joraph library.
 */
open class JoraphException : RuntimeException {
    /**
     * Creates a new instance of JoraphException.
     * @param message exception message
     */
    constructor(message: String?) : super(message) {}

    /**
     * Creates a new instance of JoraphException.
     * @param message exception message
     * @param cause the root throwable
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}

    /**
     * Creates a new instance of JoraphException.
     * @param cause the root throwable
     */
    constructor(cause: Throwable?) : super(cause) {}
}
