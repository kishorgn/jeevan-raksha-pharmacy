package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO representing a customer ranked by total spend.
 *
 * <p>Used by: GET /api/customers/top-spenders</p>
 *
 * <p>Results are ordered by {@code totalAmountSpent} descending so the
 * highest-spending customer appears first — useful for loyalty programs.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSpenderResponse {

    private Integer customerId;
    private String name;
    private String city;
    private String phone;

    /** Cumulative amount spent by this customer across all orders (INR). */
    private BigDecimal totalAmountSpent;
}
