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
 * Response DTO formatted as a pharmacy invoice.
 *
 * <p>Used by: GET /api/orders/{orderId}/invoice</p>
 *
 * <p>Designed for print-ready or PDF-generation use cases; contains
 * all fields a front-end or reporting tool would need to render a
 * complete invoice document.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    // --- Invoice Header ---
    private Integer invoiceNumber;       // same as orderId
    private LocalDate invoiceDate;       // same as orderDate

    // --- Customer Section ---
    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private String customerCity;

    // --- Line Items ---
    private List<OrderItemResponse> items;

    // --- Totals Section ---
    private BigDecimal totalAmount;
    private PaymentMode paymentMode;

    // --- Pharmacy Details (static) ---
    private String pharmacyName;
    private String pharmacyLocation;
}
