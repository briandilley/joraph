package com.joraph;

/**
 * Base exceptions used within the Joraph library.
 */
@SuppressWarnings("serial")
public class JoraphException
		extends RuntimeException {

	/**
	 * Creates a new instance of JoraphException.
	 * @param message exception message
	 */
    public JoraphException(String message) {
        super(message);
    }

	/**
	 * Creates a new instance of JoraphException.
	 * @param message exception message
	 * @param cause the root throwable
	 */
    public JoraphException(String message, Throwable cause) {
        super(message, cause);
    }

	/**
	 * Creates a new instance of JoraphException.
	 * @param cause the root throwable
	 */
    public JoraphException(Throwable cause) {
        super(cause);
    }

}
