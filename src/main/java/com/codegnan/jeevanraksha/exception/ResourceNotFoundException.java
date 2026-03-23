package com.codegnan.jeevanraksha.exception;

/**
 * Thrown when a requested entity (customer, medicine, order, supplier)
 * cannot be found in the database.
 *
 * <p>Mapped to HTTP 404 Not Found by
 * {@link GlobalExceptionHandler#handleResourceNotFound}.</p>
 *
 * <p>Usage example:
 * <pre>
 *   Customer customer = customerRepository.findById(id)
 *       .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
 * </pre>
 * </p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a descriptive not-found message using a standard template.
     *
     * @param resourceName the entity type (e.g., "Customer", "Medicine")
     * @param fieldName    the field used for lookup (e.g., "id", "name")
     * @param fieldValue   the value that was searched (e.g., 42)
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s '%s' not found", resourceName, fieldName, fieldValue));
    }

    /**
     * Creates a custom not-found message.
     *
     * @param message custom error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
