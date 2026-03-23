package com.codegnan.jeevanraksha.repository;

import com.codegnan.jeevanraksha.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Medicine} entities.
 *
 * <p>Provides all query methods needed for medicine CRUD, filtering,
 * inventory management, and reporting.</p>
 */
@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Integer> {

    // ---------------------------------------------------------------
    // Derived query methods
    // ---------------------------------------------------------------

    /**
     * Paginated list of all medicines (no filter).
     * Used by: GET /api/medicines?page=0&size=10
     */
    Page<Medicine> findAll(Pageable pageable);

    /**
     * Paginated list of medicines filtered by category (case-insensitive).
     * Used by: GET /api/medicines?category=Tablet&page=0&size=10
     */
    Page<Medicine> findByCategoryIgnoreCase(String category, Pageable pageable);

    /**
     * All medicines in a specific category (no pagination — full list for category pages).
     * Used by: GET /api/medicines/by-category/{category}
     */
    List<Medicine> findByCategoryIgnoreCase(String category);

    /**
     * All medicines supplied by a given supplier.
     * Used by: GET /api/medicines/by-supplier/{supplierId}
     *       and GET /api/suppliers/{supplierId}/medicines
     */
    List<Medicine> findBySupplierSupplierId(Integer supplierId);

    /**
     * Medicines priced between min and max (inclusive).
     * Used by: GET /api/medicines/price-range?min=30&max=150
     */
    List<Medicine> findByPriceBetween(BigDecimal min, BigDecimal max);

    /**
     * Medicines with stock at or below the given threshold.
     * Used by: GET /api/inventory/low-stock?threshold=50
     */
    List<Medicine> findByStockQuantityLessThanEqual(Integer threshold);

    /**
     * Medicines whose expiry date is before today — expired stock.
     * Used by: GET /api/reports/expired-medicines
     */
    List<Medicine> findByExpiryDateBefore(LocalDate date);

    /**
     * Full-text search on medicine name (case-insensitive contains).
     * Used by: GET /api/search?q=dolo&type=medicine
     */
    List<Medicine> findByNameContainingIgnoreCase(String query);

    /**
     * Counts how many order items reference a given medicine.
     * Used to enforce referential integrity before deleting a medicine.
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.medicine.medicineId = :medicineId")
    long countOrderItemsByMedicineId(@Param("medicineId") Integer medicineId);

    /**
     * Returns all medicines with stock below the threshold, including supplier info.
     * Used by: GET /api/reports/inventory-audit
     * Joins supplier eagerly to avoid N+1.
     */
    @Query("SELECT m FROM Medicine m JOIN FETCH m.supplier WHERE m.stockQuantity <= :threshold")
    List<Medicine> findLowStockWithSupplier(@Param("threshold") Integer threshold);
}
