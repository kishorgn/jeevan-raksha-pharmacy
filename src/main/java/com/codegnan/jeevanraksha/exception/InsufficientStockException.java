package com.codegnan.jeevanraksha.exception;

/**
 * Thrown when an order requests more units of a medicine than are
 * currently available in stock.
 *
 * <p>Mapped to HTTP 422 Unprocessable Entity by
 * {@link GlobalExceptionHandler#handleInsufficientStock}.</p>
 *
 * <p>Usage example:
 * <pre>
 *   if (medicine.getStockQuantity() < requestedQty) {
 *       throw new InsufficientStockException(
 *           medicine.getName(), medicine.getStockQuantity(), requestedQty);
 *   }
 * </pre>
 * </p>
 */
public class InsufficientStockException extends RuntimeException {

    /**
     * Creates a descriptive insufficient-stock message.
     *
     * @param medicineName  name of the medicine that is out of stock
     * @param available     units currently in stock
     * @param requested     units requested in the order
     */
    public InsufficientStockException(String medicineName, int available, int requested) {
        super(String.format(
                "Insufficient stock for '%s': requested %d unit(s), only %d available",
                medicineName, requested, available));
    }

    /**
     * Creates a custom insufficient-stock message.
     *
     * @param message custom error message
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}
