package com.codegnan.jeevanraksha.controller;

import com.codegnan.jeevanraksha.dto.request.RestockRequest;
import com.codegnan.jeevanraksha.dto.response.ApiResponse;
import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Inventory management.
 *
 * <p>Base path: {@code /api/inventory}</p>
 *
 * <p>Exposes 3 endpoints for stock overview, low-stock alerting,
 * and delta-based restocking.</p>
 *
 * <p>Uses {@code PATCH} (not {@code PUT}) for restock because only the
 * {@code stock_quantity} field is modified — a partial update.</p>
 */
@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Stock tracking and restocking alerts")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ---------------------------------------------------------------
    // GET /api/inventory
    // ---------------------------------------------------------------

    /**
     * Complete stock overview for all medicines.
     */
    @GetMapping
    @Operation(summary = "Full inventory overview",
               description = "Returns stock information for every medicine in the catalogue.")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAllInventory() {
        logger.info("GET /api/inventory");
        List<MedicineResponse> result = inventoryService.getAllInventory();
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/inventory/low-stock
    // ---------------------------------------------------------------

    /**
     * Medicines below a stock threshold — triggers restock alerts.
     *
     * <p>Sample: {@code GET /api/inventory/low-stock?threshold=50}</p>
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Low-stock alert",
               description = "Returns medicines at or below the given stock threshold (default: 50).")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getLowStockMedicines(
            @Parameter(description = "Stock threshold; medicines at or below this level are returned", example = "50")
            @RequestParam(required = false) Integer threshold) {
        logger.info("GET /api/inventory/low-stock | threshold={}", threshold);
        List<MedicineResponse> result = inventoryService.getLowStockMedicines(threshold);
        return ResponseEntity.ok(ApiResponse.success("Low-stock medicines retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // PATCH /api/inventory/{medicineId}/restock
    // ---------------------------------------------------------------

    /**
     * Update stock after a new shipment — delta quantity.
     *
     * <p>The {@code quantity} in the request body is the number of units to
     * <em>add</em> to the current stock (delta), not a replacement value.</p>
     */
    @PatchMapping("/{medicineId}/restock")
    @Operation(summary = "Restock a medicine",
               description = "Adds the specified quantity to the current stock (delta restock). " +
                             "newStock = currentStock + quantity.")
    public ResponseEntity<ApiResponse<MedicineResponse>> restockMedicine(
            @PathVariable Integer medicineId,
            @Valid @RequestBody RestockRequest request) {
        logger.info("PATCH /api/inventory/{}/restock | qty={}", medicineId, request.getQuantity());
        MedicineResponse result = inventoryService.restockMedicine(medicineId, request);
        return ResponseEntity.ok(ApiResponse.success("Medicine restocked successfully", result));
    }
}
