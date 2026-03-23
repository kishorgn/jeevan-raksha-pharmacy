package com.codegnan.jeevanraksha.controller;

import com.codegnan.jeevanraksha.dto.request.OrderRequest;
import com.codegnan.jeevanraksha.dto.response.ApiResponse;
import com.codegnan.jeevanraksha.dto.response.InvoiceResponse;
import com.codegnan.jeevanraksha.dto.response.OrderResponse;
import com.codegnan.jeevanraksha.enums.PaymentMode;
import com.codegnan.jeevanraksha.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for Order management.
 *
 * <p>Base path: {@code /api/orders}</p>
 *
 * <p>Exposes 7 endpoints for order placement, retrieval, date-range filtering,
 * cancellation, payment-mode filtering, and invoice generation.</p>
 *
 * <p><strong>Note on path ordering:</strong> fixed sub-paths
 * ({@code /by-date-range}, {@code /by-payment-mode}) are declared before
 * {@code /{orderId}} to prevent path-variable collisions.</p>
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order placement, history, invoices, and cancellation")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ---------------------------------------------------------------
    // GET /api/orders
    // ---------------------------------------------------------------

    /**
     * Admin: all orders with date-range filter and pagination.
     *
     * <p>Sample: {@code GET /api/orders?from=2023-10-01&to=2023-10-31}</p>
     */
    @GetMapping
    @Operation(summary = "List all orders",
               description = "Returns a paginated list of all orders. Optionally filter by date range.")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2023-10-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date (yyyy-MM-dd)", example = "2023-10-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/orders | from={} to={} page={} size={}", from, to, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<OrderResponse> result = orderService.getAllOrders(from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/orders/by-date-range
    // ---------------------------------------------------------------

    /**
     * Orders in a calendar window — for monthly sales reports.
     */
    @GetMapping("/by-date-range")
    @Operation(summary = "Orders by date range",
               description = "Returns all orders placed within an inclusive date window.")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        logger.info("GET /api/orders/by-date-range | from={} to={}", from, to);
        List<OrderResponse> result = orderService.getOrdersByDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/orders/by-payment-mode/{mode}
    // ---------------------------------------------------------------

    /**
     * All orders for a given payment mode.
     *
     * <p>Sample: {@code GET /api/orders/by-payment-mode/UPI}</p>
     */
    @GetMapping("/by-payment-mode/{mode}")
    @Operation(summary = "Orders by payment mode",
               description = "Returns all orders settled with the specified payment mode (UPI, Cash, Card).")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByPaymentMode(
            @PathVariable PaymentMode mode) {
        logger.info("GET /api/orders/by-payment-mode/{}", mode);
        List<OrderResponse> result = orderService.getOrdersByPaymentMode(mode);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/orders/{orderId}
    // ---------------------------------------------------------------

    /**
     * Full order detail with line items — order confirmation.
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get an order",
               description = "Returns full order details including all line items.")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Integer orderId) {
        logger.info("GET /api/orders/{}", orderId);
        OrderResponse result = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/orders/{orderId}/invoice
    // ---------------------------------------------------------------

    /**
     * Invoice-ready JSON for a completed order.
     */
    @GetMapping("/{orderId}/invoice")
    @Operation(summary = "Get order invoice",
               description = "Returns invoice-formatted JSON for an order, ready for PDF rendering.")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getOrderInvoice(
            @PathVariable Integer orderId) {
        logger.info("GET /api/orders/{}/invoice", orderId);
        InvoiceResponse result = orderService.getInvoice(orderId);
        return ResponseEntity.ok(ApiResponse.success("Invoice retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // POST /api/orders
    // ---------------------------------------------------------------

    /**
     * Place a new order — validates stock, deducts quantities.
     */
    @PostMapping
    @Operation(summary = "Place an order",
               description = "Places a new order. Validates stock availability and deducts quantities atomically.")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest request) {
        logger.info("POST /api/orders | customerId={} items={}", request.getCustomerId(),
                request.getItems().size());
        OrderResponse result = orderService.placeOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", result));
    }

    // ---------------------------------------------------------------
    // DELETE /api/orders/{orderId}
    // ---------------------------------------------------------------

    /**
     * Cancel order — restores stock quantities automatically.
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel an order",
               description = "Cancels an order and restores the stock quantity for each medicine in the order.")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Integer orderId) {
        logger.info("DELETE /api/orders/{}", orderId);
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled and stock restored successfully"));
    }
}
