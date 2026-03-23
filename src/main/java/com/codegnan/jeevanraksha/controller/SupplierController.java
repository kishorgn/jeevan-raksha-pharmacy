package com.codegnan.jeevanraksha.controller;

import com.codegnan.jeevanraksha.dto.request.SupplierRequest;
import com.codegnan.jeevanraksha.dto.response.ApiResponse;
import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.dto.response.SupplierDetailResponse;
import com.codegnan.jeevanraksha.dto.response.SupplierResponse;
import com.codegnan.jeevanraksha.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Supplier management.
 *
 * <p>Base path: {@code /api/suppliers}</p>
 *
 * <p>Exposes 6 endpoints for supplier CRUD operations and
 * medicine-by-supplier lookup.</p>
 */
@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Supplier management and medicine catalogue")
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // ---------------------------------------------------------------
    // GET /api/suppliers
    // ---------------------------------------------------------------

    /**
     * List all suppliers with medicine count.
     */
    @GetMapping
    @Operation(summary = "List all suppliers",
               description = "Returns all suppliers with the count of medicines each supplies.")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {
        logger.info("GET /api/suppliers");
        List<SupplierResponse> result = supplierService.getAllSuppliers();
        return ResponseEntity.ok(ApiResponse.success("Suppliers retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/suppliers/{supplierId}
    // ---------------------------------------------------------------

    /**
     * Supplier profile with all medicines and average price.
     */
    @GetMapping("/{supplierId}")
    @Operation(summary = "Get supplier profile",
               description = "Returns a supplier's full profile including all supplied medicines and average price.")
    public ResponseEntity<ApiResponse<SupplierDetailResponse>> getSupplierById(
            @PathVariable Integer supplierId) {
        logger.info("GET /api/suppliers/{}", supplierId);
        SupplierDetailResponse result = supplierService.getSupplierById(supplierId);
        return ResponseEntity.ok(ApiResponse.success("Supplier retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // POST /api/suppliers
    // ---------------------------------------------------------------

    /**
     * Register a new supplier.
     */
    @PostMapping
    @Operation(summary = "Register a supplier",
               description = "Creates a new supplier record. Returns 201 Created on success.")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody SupplierRequest request) {
        logger.info("POST /api/suppliers | name={}", request.getSupplierName());
        SupplierResponse result = supplierService.createSupplier(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier registered successfully", result));
    }

    // ---------------------------------------------------------------
    // PUT /api/suppliers/{supplierId}
    // ---------------------------------------------------------------

    /**
     * Update supplier name or contact details.
     */
    @PutMapping("/{supplierId}")
    @Operation(summary = "Update a supplier",
               description = "Updates the supplier name, contact person, or phone number.")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @PathVariable Integer supplierId,
            @Valid @RequestBody SupplierRequest request) {
        logger.info("PUT /api/suppliers/{}", supplierId);
        SupplierResponse result = supplierService.updateSupplier(supplierId, request);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", result));
    }

    // ---------------------------------------------------------------
    // DELETE /api/suppliers/{supplierId}
    // ---------------------------------------------------------------

    /**
     * Remove supplier — blocked if medicines are linked.
     */
    @DeleteMapping("/{supplierId}")
    @Operation(summary = "Delete a supplier",
               description = "Deletes a supplier. Returns 409 Conflict if medicines are still linked.")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(
            @PathVariable Integer supplierId) {
        logger.info("DELETE /api/suppliers/{}", supplierId);
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted successfully"));
    }

    // ---------------------------------------------------------------
    // GET /api/suppliers/{supplierId}/medicines
    // ---------------------------------------------------------------

    /**
     * All medicines supplied by this supplier.
     *
     * <p>Sample: {@code GET /api/suppliers/1/medicines}</p>
     */
    @GetMapping("/{supplierId}/medicines")
    @Operation(summary = "Medicines by supplier",
               description = "Returns all medicines provided by the specified supplier.")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getMedicinesBySupplier(
            @PathVariable Integer supplierId) {
        logger.info("GET /api/suppliers/{}/medicines", supplierId);
        List<MedicineResponse> result = supplierService.getMedicinesBySupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success("Medicines retrieved successfully", result));
    }
}
