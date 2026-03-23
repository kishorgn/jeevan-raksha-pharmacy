package com.codegnan.jeevanraksha.repository;

import com.codegnan.jeevanraksha.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Supplier} entities.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    // ---------------------------------------------------------------
    // Custom JPQL queries
    // ---------------------------------------------------------------

    /**
     * Counts the number of medicines supplied by a given supplier.
     *
     * <p>Used in two places:
     * <ul>
     *   <li>List suppliers (GET /api/suppliers) — to include medicineCount per supplier.</li>
     *   <li>Delete supplier (DELETE /api/suppliers/{supplierId}) — to block deletion if medicines exist.</li>
     * </ul>
     * </p>
     */
    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.supplier.supplierId = :supplierId")
    long countMedicinesBySupplierId(@Param("supplierId") Integer supplierId);

    /**
     * Calculates the average retail price of all medicines supplied by a given supplier.
     *
     * <p>Used by: GET /api/suppliers/{supplierId} (supplier detail profile).</p>
     *
     * @return average price as BigDecimal, or null if no medicines exist
     */
    @Query("SELECT AVG(m.price) FROM Medicine m WHERE m.supplier.supplierId = :supplierId")
    BigDecimal findAvgPriceBySupplierId(@Param("supplierId") Integer supplierId);

    /**
     * Returns all suppliers together with their medicine counts in a single query.
     *
     * <p>More efficient than N+1 queries for building the supplier list response.
     * Used by: GET /api/suppliers.</p>
     *
     * @return list of Object arrays where:
     *         [0] = Supplier entity,
     *         [1] = Long medicineCount
     */
    @Query("SELECT s, COUNT(m) AS medicineCount " +
           "FROM Supplier s LEFT JOIN s.medicines m " +
           "GROUP BY s")
    List<Object[]> findAllSuppliersWithMedicineCount();

    /**
     * Searches for suppliers whose name contains the given query string (case-insensitive).
     *
     * <p>Used by: GET /api/search?q=apollo&type=supplier</p>
     */
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Supplier> searchByName(@Param("query") String query);
}
