package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.dto.response.SearchResponse;
import com.codegnan.jeevanraksha.dto.response.SupplierResponse;
import com.codegnan.jeevanraksha.entity.Supplier;
import com.codegnan.jeevanraksha.exception.InvalidRequestException;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import com.codegnan.jeevanraksha.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for the cross-entity search endpoint.
 *
 * <p>Searches medicines (by name) and/or suppliers (by supplier name)
 * based on the {@code q} query parameter and {@code type} filter.</p>
 *
 * <p>Valid values for {@code type}: {@code "all"}, {@code "medicine"},
 * {@code "supplier"} (case-insensitive).</p>
 */
@Service
@Transactional(readOnly = true)
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private static final Set<String> VALID_TYPES = Set.of("all", "medicine", "supplier");

    private final MedicineRepository medicineRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineService medicineService;
    private final SupplierService supplierService;

    public SearchService(MedicineRepository medicineRepository,
                         SupplierRepository supplierRepository,
                         MedicineService medicineService,
                         SupplierService supplierService) {
        this.medicineRepository = medicineRepository;
        this.supplierRepository = supplierRepository;
        this.medicineService = medicineService;
        this.supplierService = supplierService;
    }

    // ---------------------------------------------------------------
    // GET /api/search?q=dolo&type=all
    // ---------------------------------------------------------------

    /**
     * Performs a case-insensitive full-text search across medicines and suppliers.
     *
     * @param query the search term (must not be blank)
     * @param type  entity scope: "all", "medicine", or "supplier" (default: "all")
     * @return aggregated search results
     * @throws InvalidRequestException if {@code query} is blank or {@code type} is unknown
     */
    public SearchResponse search(String query, String type) {
        if (query == null || query.isBlank()) {
            throw new InvalidRequestException("Search query 'q' must not be empty");
        }

        String resolvedType = (type == null || type.isBlank()) ? "all" : type.toLowerCase();
        if (!VALID_TYPES.contains(resolvedType)) {
            throw new InvalidRequestException(
                    String.format("Invalid search type '%s'. Valid values are: all, medicine, supplier", type));
        }

        logger.debug("Searching | query: '{}' | type: '{}'", query, resolvedType);

        List<MedicineResponse> medicines = Collections.emptyList();
        List<SupplierResponse> suppliers = Collections.emptyList();

        // Medicines search
        if ("all".equals(resolvedType) || "medicine".equals(resolvedType)) {
            medicines = medicineRepository.findByNameContainingIgnoreCase(query)
                    .stream()
                    .map(medicineService::toResponse)
                    .collect(Collectors.toList());
        }

        // Suppliers search
        if ("all".equals(resolvedType) || "supplier".equals(resolvedType)) {
            List<Supplier> supplierResults = supplierRepository.searchByName(query);
            suppliers = supplierResults.stream()
                    .map(s -> {
                        long count = supplierRepository.countMedicinesBySupplierId(s.getSupplierId());
                        return supplierService.toResponseWithCount(s, count);
                    })
                    .collect(Collectors.toList());
        }

        int totalResults = medicines.size() + suppliers.size();
        logger.debug("Search complete | {} medicine(s) | {} supplier(s)", medicines.size(), suppliers.size());

        return SearchResponse.builder()
                .query(query)
                .type(resolvedType)
                .medicines(medicines)
                .suppliers(suppliers)
                .totalResults(totalResults)
                .build();
    }
}
