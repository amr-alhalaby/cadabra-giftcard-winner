package org.cadabra.exception;

/**
 * Thrown when fetching users from the external API fails.
 */
public class UserApiFetchException extends RuntimeException {

    public UserApiFetchException(String message) {
        super(message);
    }

    public UserApiFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}

