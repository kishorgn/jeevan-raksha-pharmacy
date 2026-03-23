package com.codegnan.jeevanraksha.dto.response;

import com.codegnan.jeevanraksha.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for a complete order, including all line items.
 *
 * <p>Used by: GET /api/orders/{orderId} and GET /api/customers/{customerId}/orders.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Integer orderId;
    private Integer customerId;
    private String customerName;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private PaymentMode paymentMode;

    /** Individual medicine line items within this order. */
    private List<OrderItemResponse> items;
}
