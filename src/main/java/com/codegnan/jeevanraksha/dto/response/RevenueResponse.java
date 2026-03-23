package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for total revenue within a specified date range.
 *
 * <p>Used by: GET /api/reports/revenue?from=&to=</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueResponse {

    /** Start date of the reporting period (inclusive). */
    private LocalDate from;

    /** End date of the reporting period (inclusive). */
    private LocalDate to;

    /** Total revenue (sum of all order total_amounts) in the given range (INR). */
    private BigDecimal totalRevenue;

    /** Number of orders placed in the date range. */
    private long orderCount;
}
