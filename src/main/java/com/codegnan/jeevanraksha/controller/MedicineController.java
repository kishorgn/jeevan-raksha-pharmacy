package com.codegnan.jeevanraksha.controller;

import com.codegnan.jeevanraksha.dto.request.MedicineRequest;
import com.codegnan.jeevanraksha.dto.response.ApiResponse;
import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for Medicine management.
 *
 * <p>Base path: {@code /api/medicines}</p>
 *
 * <p>Exposes 8 endpoints covering the full medicine lifecycle plus
 * category, supplier, and price-range filters.</p>
 *
 * <p><strong>Note on path ordering:</strong> fixed sub-paths
 * ({@code /by-category}, {@code /by-supplier}, {@code /price-range})
 * are declared before the variable path ({@code /{medicineId}}) to
 * prevent Spring from treating them as IDs.</p>
 */
@RestController
@RequestMapping("/api/medicines")
@Tag(name = "Medicines", description = "Medicine catalogue — CRUD, filters, and expiry")
@Validated
public class MedicineController {

    private static final Logger logger = LoggerFactory.getLogger(MedicineController.class);

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    // ---------------------------------------------------------------
    // GET /api/medicines
    // ---------------------------------------------------------------

    /**
     * List all medicines — filterable by category and paginated.
     *
     * <p>Sample: {@code GET /api/medicines?category=Tablet&page=0&size=10}</p>
     */
    @GetMapping
    @Operation(summary = "List all medicines",
               description = "Returns a paginated medicine list. Optional category filter.")
    public ResponseEntity<ApiResponse<Page<MedicineResponse>>> getAllMedicines(
            @Parameter(description = "Filter by category (e.g., Tablet, Syrup, Injection)")
            @RequestParam(required = false) String category,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/medicines | category={} page={} size={}", category, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<MedicineResponse> result = medicineService.getAllMedicines(category, pageable);
        return ResponseEntity.ok(ApiResponse.success("Medicines retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/medicines/by-category/{category}
    // ---------------------------------------------------------------

    /**
     * All medicines in a specific category.
     *
     * <p>Sample: {@code GET /api/medicines/by-category/Tablet}</p>
     */
    @GetMapping("/by-category/{category}")
    @Operation(summary = "Medicines by category",
               description = "Returns all medicines matching the specified category (e.g., Tablet, Syrup).")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getMedicinesByCategory(
            @PathVariable String category) {
        logger.info("GET /api/medicines/by-category/{}", category);
        List<MedicineResponse> result = medicineService.getMedicinesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Medicines retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/medicines/by-supplier/{supplierId}
    // ---------------------------------------------------------------

    /**
     * All medicines from a specific supplier.
     *
     * <p>Sample: {@code GET /api/medicines/by-supplier/2}</p>
     */
    @GetMapping("/by-supplier/{supplierId}")
    @Operation(summary = "Medicines by supplier",
               description = "Returns all medicines provided by the specified supplier.")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getMedicinesBySupplier(
            @PathVariable Integer supplierId) {
        logger.info("GET /api/medicines/by-supplier/{}", supplierId);
        List<MedicineResponse> result = medicineService.getMedicinesBySupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success("Medicines retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/medicines/price-range
    // ---------------------------------------------------------------

    /**
     * Filter medicines between min and max price.
     *
     * <p>Sample: {@code GET /api/medicines/price-range?min=30&max=150}</p>
     */
    @GetMapping("/price-range")
    @Operation(summary = "Medicines by price range",
               description = "Returns medicines whose price falls between min and max (inclusive).")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getMedicinesByPriceRange(
            @Parameter(description = "Minimum price (INR)", example = "30")
            @RequestParam
            @DecimalMin(value = "0.0", message = "Minimum price must be non-negative")
            BigDecimal min,

            @Parameter(description = "Maximum price (INR)", example = "150")
            @RequestParam
            @DecimalMin(value = "0.0", message = "Maximum price must be non-negative")
            BigDecimal max) {

        logger.info("GET /api/medicines/price-range | min={} max={}", min, max);
        List<MedicineResponse> result = medicineService.getMedicinesByPriceRange(min, max);
        return ResponseEntity.ok(ApiResponse.success("Medicines retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // GET /api/medicines/{medicineId}
    // ---------------------------------------------------------------

    /**
     * Get full details of a single medicine.
     */
    @GetMapping("/{medicineId}")
    @Operation(summary = "Get a medicine",
               description = "Returns the full details of a single medicine by ID.")
    public ResponseEntity<ApiResponse<MedicineResponse>> getMedicineById(
            @PathVariable Integer medicineId) {
        logger.info("GET /api/medicines/{}", medicineId);
        MedicineResponse result = medicineService.getMedicineById(medicineId);
        return ResponseEntity.ok(ApiResponse.success("Medicine retrieved successfully", result));
    }

    // ---------------------------------------------------------------
    // POST /api/medicines
    // ---------------------------------------------------------------

    /**
     * Add a new medicine to the inventory.
     */
    @PostMapping
    @Operation(summary = "Add a medicine",
               description = "Adds a new medicine to the pharmacy catalogue. Returns 201 Created.")
    public ResponseEntity<ApiResponse<MedicineResponse>> createMedicine(
            @Valid @RequestBody MedicineRequest request) {
        logger.info("POST /api/medicines | name={}", request.getName());
        MedicineResponse result = medicineService.createMedicine(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medicine added successfully", result));
    }

    // ---------------------------------------------------------------
    // PUT /api/medicines/{medicineId}
    // ---------------------------------------------------------------

    /**
     * Update medicine details — price, stock, expiry.
     */
    @PutMapping("/{medicineId}")
    @Operation(summary = "Update a medicine",
               description = "Updates all fields of an existing medicine.")
    public ResponseEntity<ApiResponse<MedicineResponse>> updateMedicine(
            @PathVariable Integer medicineId,
            @Valid @RequestBody MedicineRequest request) {
        logger.info("PUT /api/medicines/{}", medicineId);
        MedicineResponse result = medicineService.updateMedicine(medicineId, request);
        return ResponseEntity.ok(ApiResponse.success("Medicine updated successfully", result));
    }

    // ---------------------------------------------------------------
    // DELETE /api/medicines/{medicineId}
    // ---------------------------------------------------------------

    /**
     * Remove a medicine from inventory.
     */
    @DeleteMapping("/{medicineId}")
    @Operation(summary = "Delete a medicine",
               description = "Removes a medicine. Returns 409 Conflict if it is referenced in orders.")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(
            @PathVariable Integer medicineId) {
        logger.info("DELETE /api/medicines/{}", medicineId);
        medicineService.deleteMedicine(medicineId);
        return ResponseEntity.ok(ApiResponse.success("Medicine deleted successfully"));
    }
}
