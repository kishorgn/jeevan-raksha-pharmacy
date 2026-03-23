package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for a single line item within an order.
 *
 * <p>Embedded inside {@link OrderResponse} and {@link InvoiceResponse}.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Integer itemId;
    private Integer medicineId;
    private String medicineName;
    private String medicineCategory;
    private Integer quantity;

    /** Price per unit at the time the order was placed (INR). */
    private BigDecimal unitPrice;

    /** Computed subtotal: unitPrice × quantity (INR). */
    private BigDecimal subtotal;
}
