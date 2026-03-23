package com.codegnan.jeevanraksha.repository;

import com.codegnan.jeevanraksha.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link OrderItem} entities.
 *
 * <p>Provides query methods for retrieving line items within
 * specific orders and computing bestseller analytics.</p>
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    // ---------------------------------------------------------------
    // Derived query methods
    // ---------------------------------------------------------------

    /**
     * All line items belonging to a given order.
     * Used when building an order detail or invoice response.
     */
    List<OrderItem> findByOrderOrderId(Integer orderId);

    // ---------------------------------------------------------------
    // Custom JPQL queries
    // ---------------------------------------------------------------

    /**
     * Top medicines ranked by total quantity sold across all orders.
     * Used by: GET /api/reports/bestsellers
     *
     * <p>Joins the medicine entity to include id, name, category, and price
     * alongside the aggregate figures.</p>
     *
     * @return list of Object arrays where:
     *         [0] = Integer medicineId,
     *         [1] = String medicineName,
     *         [2] = String medicineCategory,
     *         [3] = BigDecimal medicinePrice,
     *         [4] = Long totalQuantitySold,
     *         [5] = BigDecimal totalRevenue (sum of subtotals)
     */
    @Query("SELECT oi.medicine.medicineId, " +
           "       oi.medicine.name, " +
           "       oi.medicine.category, " +
           "       oi.medicine.price, " +
           "       SUM(oi.quantity), " +
           "       SUM(oi.subtotal) " +
           "FROM OrderItem oi " +
           "GROUP BY oi.medicine.medicineId, oi.medicine.name, oi.medicine.category, oi.medicine.price " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findBestsellers();
}
