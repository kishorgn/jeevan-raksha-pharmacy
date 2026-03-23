package com.codegnan.jeevanraksha.controller;

import com.codegnan.jeevanraksha.dto.request.CustomerRequest;
import com.codegnan.jeevanraksha.dto.response.*;
import com.codegnan.jeevanraksha.service.CustomerService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Customer management.
 *
 * <p>Base path: {@code /api/customers}</p>
 *
 * <p>Exposes 7 endpoints covering customer registration, profile retrieval,
 * update, deletion (with integrity check), order history, and top-spender
 * analytics.</p>
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer registration, profiles, and order history")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ---------------------------------------------------------------
    // GET /api/customers
    // ---------------------------------------------------------------

    /**
     * List all customers — filterable by city and paginated.
     *
     * <p>Sample: {@code GET /api/customers?city=Mumbai&page=0&size=10}</p>
     */
    @GetMapping
    @Operation(summary = "List all customers",
               description = "Returns a paginated list of customers. Optionally filter by city.")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getAllCustomers(
            @Parameter(description = "Filter by city name (case-insensitive)")
            @RequestParam(required = false) String city,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/customers | city={} page={} size={}", city, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<CustomerResponse> result = customerService.getAllCustomers(city, pageable);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/customers/top-spenders
    // NOTE: This mapping MUST be declared before /{customerId} to avoid
    //       Spring mistaking "top-spenders" for a path variable.
    // ---------------------------------------------------------------

    /**
     * Top customers by total amount spent — for loyalty programs.
     */
    @GetMapping("/top-spenders")
    @Operation(summary = "Top customers by spending",
               description = "Returns all customers ranked by cumulative order spend, highest first.")
    public ResponseEntity<ApiResponse<List<TopSpenderResponse>>> getTopSpenders() {
        logger.info("GET /api/customers/top-spenders");
        List<TopSpenderResponse> result = customerService.getTopSpenders();
        return ResponseEntity.ok(ApiResponse.success("Top spenders retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/customers/{customerId}
    // ---------------------------------------------------------------

    /**
     * Customer profile with order summary.
     */
    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer profile",
               description = "Returns a customer's profile with total orders and cumulative spend.")
    public ResponseEntity<ApiResponse<CustomerSummaryResponse>> getCustomerById(
            @PathVariable Integer customerId) {
        logger.info("GET /api/customers/{}", customerId);
        CustomerSummaryResponse result = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // POST /api/customers
    // ---------------------------------------------------------------

    /**
     * Register a new customer account.
     */
    @PostMapping
    @Operation(summary = "Register a customer",
               description = "Creates a new customer account. Returns 201 Created on success.")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        logger.info("POST /api/customers | name={}", request.getName());
        CustomerResponse result = customerService.createCustomer(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registered successfully", result));
    }

    // ---------------------------------------------------------------
    // PUT /api/customers/{customerId}
    // ---------------------------------------------------------------

    /**
     * Update customer profile — city or phone change.
     */
    @PutMapping("/{customerId}")
    @Operation(summary = "Update a customer",
               description = "Updates all fields of an existing customer profile.")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Integer customerId,
            @Valid @RequestBody CustomerRequest request) {
        logger.info("PUT /api/customers/{}", customerId);
        CustomerResponse result = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", result));
    }

    // ---------------------------------------------------------------
    // DELETE /api/customers/{customerId}
    // ---------------------------------------------------------------

    /**
     * Remove customer — blocked if orders are linked.
     */
    @DeleteMapping("/{customerId}")
    @Operation(summary = "Delete a customer",
               description = "Deletes a customer. Returns 409 Conflict if orders exist for this customer.")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable Integer customerId) {
        logger.info("DELETE /api/customers/{}", customerId);
        customerService.deleteCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully"));
    }

    // ---------------------------------------------------------------
    // GET /api/customers/{customerId}/orders
    // ---------------------------------------------------------------

    /**
     * Full order history for a customer — My Orders page.
     */
    @GetMapping("/{customerId}/orders")
    @Operation(summary = "Customer order history",
               description = "Returns the complete order history for a given customer.")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getCustomerOrders(
            @PathVariable Integer customerId) {
        logger.info("GET /api/customers/{}/orders", customerId);
        List<OrderResponse> result = customerService.getCustomerOrders(customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer orders retrieved successfully", result));
    }
}
