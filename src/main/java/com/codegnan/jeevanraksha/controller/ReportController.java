package com.codegnan.jeevanraksha.controller;

import com.codegnan.jeevanraksha.dto.response.*;
import com.codegnan.jeevanraksha.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Analytics and Reports.
 *
 * <p>Base path: {@code /api/reports}</p>
 *
 * <p>Exposes 6 read-only endpoints for revenue analysis, bestsellers,
 * customer insights, expired stock, and inventory auditing.</p>
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Revenue, bestsellers, and analytics")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ---------------------------------------------------------------
    // GET /api/reports/revenue
    // ---------------------------------------------------------------

    /**
     * Total revenue over a date range — core business KPI.
     *
     * <p>Sample: {@code GET /api/reports/revenue?from=2023-01-01&to=2023-12-31}</p>
     */
    @GetMapping("/revenue")
    @Operation(summary = "Total revenue",
               description = "Calculates the total pharmacy revenue within an inclusive date range.")
    public ResponseEntity<ApiResponse<RevenueResponse>> getRevenue(
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2023-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date (yyyy-MM-dd)", example = "2023-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        logger.info("GET /api/reports/revenue | from={} to={}", from, to);
        RevenueResponse result = reportService.getRevenue(from, to);
        return ResponseEntity.ok(ApiResponse.success("Revenue report generated successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/reports/revenue-by-payment-mode
    // ---------------------------------------------------------------

    /**
     * Revenue breakdown per payment mode — cash-flow analysis.
     */
    @GetMapping("/revenue-by-payment-mode")
    @Operation(summary = "Revenue by payment mode",
               description = "Returns total revenue and order count grouped by UPI, Cash, and Card.")
    public ResponseEntity<ApiResponse<List<RevenueByModeResponse>>> getRevenueByPaymentMode() {
        logger.info("GET /api/reports/revenue-by-payment-mode");
        List<RevenueByModeResponse> result = reportService.getRevenueByPaymentMode();
        return ResponseEntity.ok(ApiResponse.success("Revenue by payment mode retrieved", result));
    }

    // ---------------------------------------------------------------
    // GET /api/reports/bestsellers
    // ---------------------------------------------------------------

    /**
     * Top medicines by quantity sold — full analytics data.
     */
    @GetMapping("/bestsellers")
    @Operation(summary = "Bestselling medicines",
               description = "Returns all medicines ranked by total units sold across all orders.")
    public ResponseEntity<ApiResponse<List<BestsellerResponse>>> getBestsellers() {
        logger.info("GET /api/reports/bestsellers");
        List<BestsellerResponse> result = reportService.getBestsellers();
        return ResponseEntity.ok(ApiResponse.success("Bestsellers retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/reports/customer-with-most-orders
    // ---------------------------------------------------------------

    /**
     * Customer with most orders — VIP identification.
     */
    @GetMapping("/customer-with-most-orders")
    @Operation(summary = "Customer with most orders",
               description = "Returns the customer who has placed the highest number of orders.")
    public ResponseEntity<ApiResponse<CustomerOrderCountResponse>> getCustomerWithMostOrders() {
        logger.info("GET /api/reports/customer-with-most-orders");
        CustomerOrderCountResponse result = reportService.getCustomerWithMostOrders();
        return ResponseEntity.ok(ApiResponse.success("Customer with most orders retrieved", result));
    }

    // ---------------------------------------------------------------
    // GET /api/reports/expired-medicines
    // ---------------------------------------------------------------

    /**
     * Medicines past expiry date — compliance audit.
     */
    @GetMapping("/expired-medicines")
    @Operation(summary = "Expired medicines",
               description = "Returns all medicines whose expiry date is before today.")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getExpiredMedicines() {
        logger.info("GET /api/reports/expired-medicines");
        List<MedicineResponse> result = reportService.getExpiredMedicines();
        return ResponseEntity.ok(ApiResponse.success("Expired medicines retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/reports/inventory-audit
    // ---------------------------------------------------------------

    /**
     * Low-stock medicines with supplier contact — reorder sheet.
     */
    @GetMapping("/inventory-audit")
    @Operation(summary = "Inventory audit report",
               description = "Returns low-stock medicines (≤50 units) with supplier contact details " +
                             "for generating reorder sheets.")
    public ResponseEntity<ApiResponse<List<InventoryAuditResponse>>> getInventoryAudit() {
        logger.info("GET /api/reports/inventory-audit");
        List<InventoryAuditResponse> result = reportService.getInventoryAudit();
        return ResponseEntity.ok(ApiResponse.success("Inventory audit report generated", result));
    }
}
