package com.codegnan.jeevanraksha.repository;

import com.codegnan.jeevanraksha.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Customer} entities.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations,
 * and declares additional custom JPQL queries for business-specific
 * lookups required by the REST API.</p>
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // ---------------------------------------------------------------
    // Derived query methods (Spring Data auto-implements these)
    // ---------------------------------------------------------------

    /**
     * Returns a paginated list of customers filtered by city (case-insensitive).
     *
     * <p>Used by: GET /api/customers?city=Mumbai&page=0&size=10</p>
     */
    Page<Customer> findByCityIgnoreCase(String city, Pageable pageable);

    // ---------------------------------------------------------------
    // Custom JPQL queries
    // ---------------------------------------------------------------

    /**
     * Returns all customers ranked by their cumulative spending, highest first.
     *
     * <p>LEFT JOIN ensures customers with zero orders are also included
     * (with totalSpent = 0). COALESCE handles NULL sum for such cases.</p>
     *
     * <p>Used by: GET /api/customers/top-spenders</p>
     *
     * @return list of Object arrays where:
     *         [0] = Customer entity,
     *         [1] = BigDecimal totalSpent
     */
    @Query("SELECT c, COALESCE(SUM(o.totalAmount), 0) AS totalSpent " +
           "FROM Customer c LEFT JOIN c.orders o " +
           "GROUP BY c " +
           "ORDER BY totalSpent DESC")
    List<Object[]> findTopSpenders();

    /**
     * Counts how many orders exist for a given customer.
     *
     * <p>Used to enforce the referential integrity check before deleting
     * a customer (DELETE /api/customers/{customerId}).</p>
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.customerId = :customerId")
    long countOrdersByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Returns aggregate order statistics (count, total spent) for a given customer.
     *
     * <p>Used by: GET /api/customers/{customerId} (profile + order summary).</p>
     *
     * @return Object array where:
     *         [0] = Long orderCount,
     *         [1] = BigDecimal totalSpent (0 if no orders)
     */
    @Query("SELECT COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o WHERE o.customer.customerId = :customerId")
    Object[] findOrderStatsByCustomerId(@Param("customerId") Integer customerId);
}
