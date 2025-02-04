package org.crud.bookmarks.service;

/**
 * Exception thrown when a URL validation fails.
 * This can occur due to:
 * - Null or empty URL
 * - Malformed URL format
 * - Inaccessible resource (non-200 HTTP response)
 * - Connection timeout
 * - Other network-related issues
 */
public class InvalidUrlException extends RuntimeException {
    /**
     * Constructs a new InvalidUrlException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidUrlException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidUrlException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
