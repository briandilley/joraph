package com.joraph;

@SuppressWarnings("serial")
public class JoraphException
	extends RuntimeException {

    public JoraphException(String message) {
        super(message);
    }

    public JoraphException(String message, Throwable cause) {
        super(message, cause);
    }

    public JoraphException(Throwable cause) {
        super(cause);
    }

}
