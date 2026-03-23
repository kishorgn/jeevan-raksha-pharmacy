package com.codegnan.jeevanraksha.exception;

/**
 * Thrown when a delete operation is blocked due to a referential integrity
 * constraint — i.e., the record to be deleted is still referenced by other
 * records.
 *
 * <p>Mapped to HTTP 409 Conflict by
 * {@link GlobalExceptionHandler#handleResourceConstraint}.</p>
 *
 * <p>Use cases:
 * <ul>
 *   <li>Deleting a customer who has existing orders.</li>
 *   <li>Deleting a supplier who still supplies medicines.</li>
 *   <li>Deleting a medicine that is part of one or more orders.</li>
 * </ul>
 * </p>
 */
public class ResourceConstraintException extends RuntimeException {

    /**
     * @param message descriptive message explaining the constraint violation
     */
    public ResourceConstraintException(String message) {
        super(message);
    }
}
