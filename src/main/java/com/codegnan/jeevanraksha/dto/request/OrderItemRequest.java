package com.codegnan.jeevanraksha.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single line item inside an {@link OrderRequest}.
 *
 * <p>Specifies which medicine to purchase and the quantity desired.
 * Subtotal is computed server-side based on the medicine's current price.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    /** ID of the medicine to purchase. */
    @NotNull(message = "Medicine ID is required for each order item")
    @Positive(message = "Medicine ID must be a positive integer")
    private Integer medicineId;

    /** Number of units to purchase. Must be at least 1. */
    @NotNull(message = "Quantity is required for each order item")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
