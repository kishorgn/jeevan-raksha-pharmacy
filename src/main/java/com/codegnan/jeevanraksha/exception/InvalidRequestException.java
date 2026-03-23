package com.codegnan.jeevanraksha.exception;

/**
 * Thrown when request parameters or business rules fail validation
 * at the service layer, beyond what Jakarta Bean Validation covers.
 *
 * <p>Mapped to HTTP 400 Bad Request by
 * {@link GlobalExceptionHandler#handleInvalidRequest}.</p>
 *
 * <p>Examples:
 * <ul>
 *   <li>Date range where {@code from} is after {@code to}.</li>
 *   <li>Unknown {@code type} value in the search endpoint.</li>
 *   <li>Negative threshold in low-stock query.</li>
 * </ul>
 * </p>
 */
public class InvalidRequestException extends RuntimeException {

    /**
     * @param message descriptive message explaining what is invalid
     */
    public InvalidRequestException(String message) {
        super(message);
    }
}
