package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.SupplierRequest;
import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.dto.response.SupplierDetailResponse;
import com.codegnan.jeevanraksha.dto.response.SupplierResponse;
import com.codegnan.jeevanraksha.entity.Medicine;
import com.codegnan.jeevanraksha.entity.Supplier;
import com.codegnan.jeevanraksha.exception.ResourceConstraintException;
import com.codegnan.jeevanraksha.exception.ResourceNotFoundException;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import com.codegnan.jeevanraksha.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for all Supplier business logic.
 */
@Service
@Transactional(readOnly = true)
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;

    public SupplierService(SupplierRepository supplierRepository,
                           MedicineRepository medicineRepository) {
        this.supplierRepository = supplierRepository;
        this.medicineRepository = medicineRepository;
    }

    // ---------------------------------------------------------------
    // GET /api/suppliers
    // ---------------------------------------------------------------

    /**
     * Returns all suppliers with their medicine count.
     */
    public List<SupplierResponse> getAllSuppliers() {
        logger.debug("Fetching all suppliers with medicine counts");
        List<Object[]> results = supplierRepository.findAllSuppliersWithMedicineCount();
        return results.stream().map(row -> {
            Supplier s = (Supplier) row[0];
            long count = ((Number) row[1]).longValue();
            return toResponseWithCount(s, count);
        }).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // GET /api/suppliers/{supplierId}
    // ---------------------------------------------------------------

    /**
     * Returns full supplier profile with all medicines and average price.
     */
    public SupplierDetailResponse getSupplierById(Integer supplierId) {
        logger.debug("Fetching supplier detail for id: {}", supplierId);
        Supplier supplier = findSupplierOrThrow(supplierId);

        List<Medicine> medicines = medicineRepository.findBySupplierSupplierId(supplierId);
        BigDecimal avgPrice = supplierRepository.findAvgPriceBySupplierId(supplierId);

        List<MedicineResponse> medicineResponses = medicines.stream()
                .map(m -> toMedicineResponse(m, supplier))
                .collect(Collectors.toList());

        return SupplierDetailResponse.builder()
                .supplierId(supplier.getSupplierId())
                .supplierName(supplier.getSupplierName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .medicineCount(medicines.size())
                .averagePrice(avgPrice != null ? avgPrice : BigDecimal.ZERO)
                .medicines(medicineResponses)
                .build();
    }

    // ---------------------------------------------------------------
    // POST /api/suppliers
    // ---------------------------------------------------------------

    /**
     * Registers a new supplier.
     */
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        logger.info("Creating new supplier: {}", request.getSupplierName());
        Supplier supplier = Supplier.builder()
                .supplierName(request.getSupplierName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .build();
        Supplier saved = supplierRepository.save(supplier);
        logger.info("Supplier created with id: {}", saved.getSupplierId());
        return toResponseWithCount(saved, 0L);
    }

    // ---------------------------------------------------------------
    // PUT /api/suppliers/{supplierId}
    // ---------------------------------------------------------------

    /**
     * Updates an existing supplier's details.
     */
    @Transactional
    public SupplierResponse updateSupplier(Integer supplierId, SupplierRequest request) {
        logger.info("Updating supplier id: {}", supplierId);
        Supplier supplier = findSupplierOrThrow(supplierId);
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        Supplier updated = supplierRepository.save(supplier);
        long count = supplierRepository.countMedicinesBySupplierId(supplierId);
        logger.info("Supplier updated: {}", updated.getSupplierId());
        return toResponseWithCount(updated, count);
    }

    // ---------------------------------------------------------------
    // DELETE /api/suppliers/{supplierId}
    // ---------------------------------------------------------------

    /**
     * Removes a supplier record.
     *
     * <p>Blocked if any medicines are still linked to this supplier.</p>
     */
    @Transactional
    public void deleteSupplier(Integer supplierId) {
        logger.info("Attempting to delete supplier id: {}", supplierId);
        findSupplierOrThrow(supplierId);

        long medicineCount = supplierRepository.countMedicinesBySupplierId(supplierId);
        if (medicineCount > 0) {
            throw new ResourceConstraintException(
                    String.format("Cannot delete supplier id %d: %d medicine(s) are linked. " +
                                  "Please reassign or remove the medicines first.", supplierId, medicineCount));
        }

        supplierRepository.deleteById(supplierId);
        logger.info("Supplier id {} deleted successfully", supplierId);
    }

    // ---------------------------------------------------------------
    // GET /api/suppliers/{supplierId}/medicines
    // ---------------------------------------------------------------

    /**
     * Returns all medicines supplied by a given supplier.
     */
    public List<MedicineResponse> getMedicinesBySupplier(Integer supplierId) {
        logger.debug("Fetching medicines for supplier id: {}", supplierId);
        Supplier supplier = findSupplierOrThrow(supplierId);
        List<Medicine> medicines = medicineRepository.findBySupplierSupplierId(supplierId);
        return medicines.stream()
                .map(m -> toMedicineResponse(m, supplier))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private Supplier findSupplierOrThrow(Integer supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));
    }

    public SupplierResponse toResponseWithCount(Supplier supplier, long count) {
        return SupplierResponse.builder()
                .supplierId(supplier.getSupplierId())
                .supplierName(supplier.getSupplierName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .medicineCount(count)
                .build();
    }

    private MedicineResponse toMedicineResponse(Medicine m, Supplier supplier) {
        return MedicineResponse.builder()
                .medicineId(m.getMedicineId())
                .name(m.getName())
                .category(m.getCategory())
                .price(m.getPrice())
                .stockQuantity(m.getStockQuantity())
                .expiryDate(m.getExpiryDate())
                .supplierId(supplier.getSupplierId())
                .supplierName(supplier.getSupplierName())
                .build();
    }
}
