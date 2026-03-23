package com.codegnan.jeevanraksha.dto.response;

import com.codegnan.jeevanraksha.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for revenue broken down by payment mode.
 *
 * <p>Used by: GET /api/reports/revenue-by-payment-mode</p>
 *
 * <p>Returns one entry per payment mode (UPI, Cash, Card) with
 * the total revenue and order count for each mode.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueByModeResponse {

    /** The payment mode (UPI, Cash, or Card). */
    private PaymentMode paymentMode;

    /** Total revenue collected via this payment mode (INR). */
    private BigDecimal totalRevenue;

    /** Number of orders settled via this payment mode. */
    private long orderCount;
}
