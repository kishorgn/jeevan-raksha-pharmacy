package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.MedicineRequest;
import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.entity.Medicine;
import com.codegnan.jeevanraksha.entity.Supplier;
import com.codegnan.jeevanraksha.exception.ResourceConstraintException;
import com.codegnan.jeevanraksha.exception.ResourceNotFoundException;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import com.codegnan.jeevanraksha.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for all Medicine business logic.
 */
@Service
@Transactional(readOnly = true)
public class MedicineService {

    private static final Logger logger = LoggerFactory.getLogger(MedicineService.class);

    private final MedicineRepository medicineRepository;
    private final SupplierRepository supplierRepository;

    public MedicineService(MedicineRepository medicineRepository,
                           SupplierRepository supplierRepository) {
        this.medicineRepository = medicineRepository;
        this.supplierRepository = supplierRepository;
    }

    // ---------------------------------------------------------------
    // GET /api/medicines (with optional category filter)
    // ---------------------------------------------------------------

    /**
     * Returns a paginated list of medicines, optionally filtered by category.
     */
    public Page<MedicineResponse> getAllMedicines(String category, Pageable pageable) {
        logger.debug("Fetching medicines | category filter: {}", category);
        Page<Medicine> page = (category != null && !category.isBlank())
                ? medicineRepository.findByCategoryIgnoreCase(category, pageable)
                : medicineRepository.findAll(pageable);
        return page.map(this::toResponse);
    }

    // ---------------------------------------------------------------
    // GET /api/medicines/{medicineId}
    // ---------------------------------------------------------------

    public MedicineResponse getMedicineById(Integer medicineId) {
        logger.debug("Fetching medicine id: {}", medicineId);
        return toResponse(findMedicineOrThrow(medicineId));
    }

    // ---------------------------------------------------------------
    // POST /api/medicines
    // ---------------------------------------------------------------

    @Transactional
    public MedicineResponse createMedicine(MedicineRequest request) {
        logger.info("Creating medicine: {}", request.getName());
        Supplier supplier = findSupplierOrThrow(request.getSupplierId());
        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .expiryDate(request.getExpiryDate())
                .supplier(supplier)
                .build();
        Medicine saved = medicineRepository.save(medicine);
        logger.info("Medicine created with id: {}", saved.getMedicineId());
        return toResponse(saved);
    }

    // ---------------------------------------------------------------
    // PUT /api/medicines/{medicineId}
    // ---------------------------------------------------------------

    @Transactional
    public MedicineResponse updateMedicine(Integer medicineId, MedicineRequest request) {
        logger.info("Updating medicine id: {}", medicineId);
        Medicine medicine = findMedicineOrThrow(medicineId);
        Supplier supplier = findSupplierOrThrow(request.getSupplierId());
        medicine.setName(request.getName());
        medicine.setCategory(request.getCategory());
        medicine.setPrice(request.getPrice());
        medicine.setStockQuantity(request.getStockQuantity());
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setSupplier(supplier);
        Medicine updated = medicineRepository.save(medicine);
        logger.info("Medicine updated: {}", updated.getMedicineId());
        return toResponse(updated);
    }

    // ---------------------------------------------------------------
    // DELETE /api/medicines/{medicineId}
    // ---------------------------------------------------------------

    @Transactional
    public void deleteMedicine(Integer medicineId) {
        logger.info("Attempting to delete medicine id: {}", medicineId);
        findMedicineOrThrow(medicineId);

        long itemCount = medicineRepository.countOrderItemsByMedicineId(medicineId);
        if (itemCount > 0) {
            throw new ResourceConstraintException(
                    String.format("Cannot delete medicine id %d: it is referenced in %d order item(s).",
                            medicineId, itemCount));
        }

        medicineRepository.deleteById(medicineId);
        logger.info("Medicine id {} deleted successfully", medicineId);
    }

    // ---------------------------------------------------------------
    // GET /api/medicines/by-category/{category}
    // ---------------------------------------------------------------

    public List<MedicineResponse> getMedicinesByCategory(String category) {
        logger.debug("Fetching medicines by category: {}", category);
        return medicineRepository.findByCategoryIgnoreCase(category)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // GET /api/medicines/by-supplier/{supplierId}
    // ---------------------------------------------------------------

    public List<MedicineResponse> getMedicinesBySupplier(Integer supplierId) {
        logger.debug("Fetching medicines for supplier id: {}", supplierId);
        findSupplierOrThrow(supplierId); // validate supplier exists
        return medicineRepository.findBySupplierSupplierId(supplierId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // GET /api/medicines/price-range?min=&max=
    // ---------------------------------------------------------------

    public List<MedicineResponse> getMedicinesByPriceRange(BigDecimal min, BigDecimal max) {
        logger.debug("Fetching medicines in price range: {} - {}", min, max);
        return medicineRepository.findByPriceBetween(min, max)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    public Medicine findMedicineOrThrow(Integer medicineId) {
        return medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));
    }

    private Supplier findSupplierOrThrow(Integer supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));
    }

    public MedicineResponse toResponse(Medicine m) {
        return MedicineResponse.builder()
                .medicineId(m.getMedicineId())
                .name(m.getName())
                .category(m.getCategory())
                .price(m.getPrice())
                .stockQuantity(m.getStockQuantity())
                .expiryDate(m.getExpiryDate())
                .supplierId(m.getSupplier() != null ? m.getSupplier().getSupplierId() : null)
                .supplierName(m.getSupplier() != null ? m.getSupplier().getSupplierName() : null)
                .build();
    }
}
