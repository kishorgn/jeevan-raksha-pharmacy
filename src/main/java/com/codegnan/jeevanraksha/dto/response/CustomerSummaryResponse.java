package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for a customer's profile with aggregated order statistics.
 *
 * <p>Used by: GET /api/customers/{customerId} — the "customer profile" endpoint.</p>
 *
 * <p>Includes the core profile fields plus two computed metrics:
 * total number of orders placed and the cumulative amount spent,
 * which are derived from the {@code orders} table at query time.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSummaryResponse {

    private Integer customerId;
    private String name;
    private String phone;
    private String city;

    /** Total number of orders placed by this customer. */
    private long totalOrders;

    /** Total amount spent across all orders (INR). */
    private BigDecimal totalAmountSpent;
}
