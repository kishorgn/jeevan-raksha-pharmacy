package com.codegnan.jeevanraksha.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for restocking a medicine's inventory.
 *
 * <p>The {@code quantity} field represents the <em>delta</em> — the number
 * of units to <strong>add</strong> to the current stock. It is not a
 * replacement value; the service will compute:</p>
 *
 * <pre>newStock = currentStock + quantity</pre>
 *
 * <p>Used by: {@code PATCH /api/inventory/{medicineId}/restock}</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestockRequest {

    /**
     * Number of units to add to the existing stock.
     * Must be at least 1.
     */
    @NotNull(message = "Restock quantity is required")
    @Min(value = 1, message = "Restock quantity must be at least 1")
    private Integer quantity;
}
