package com.codegnan.jeevanraksha.dto.request;

import com.codegnan.jeevanraksha.enums.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for placing a new pharmacy order.
 *
 * <p>When processed, the service layer will:
 * <ol>
 *   <li>Validate that the customer exists.</li>
 *   <li>Validate that each medicine exists and has sufficient stock.</li>
 *   <li>Compute subtotals and total amount from live medicine prices.</li>
 *   <li>Deduct stock quantities atomically.</li>
 *   <li>Persist the order and its line items.</li>
 * </ol>
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    /** ID of the customer placing this order. */
    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be a positive integer")
    private Integer customerId;

    /** Payment method chosen by the customer. */
    @NotNull(message = "Payment mode is required (UPI, Cash, or Card)")
    private PaymentMode paymentMode;

    /** One or more line items specifying medicines and quantities. */
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
}
