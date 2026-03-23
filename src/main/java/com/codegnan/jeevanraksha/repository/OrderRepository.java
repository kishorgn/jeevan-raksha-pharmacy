package com.codegnan.jeevanraksha.repository;

import com.codegnan.jeevanraksha.entity.Order;
import com.codegnan.jeevanraksha.enums.PaymentMode;
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
 * Spring Data JPA repository for {@link Order} entities.
 *
 * <p>Provides all query methods needed for order management,
 * filtering, invoicing, and revenue reporting.</p>
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // ---------------------------------------------------------------
    // Derived query methods
    // ---------------------------------------------------------------

    /**
     * All orders placed by a given customer.
     * Used by: GET /api/customers/{customerId}/orders
     */
    List<Order> findByCustomerCustomerId(Integer customerId);

    /**
     * All orders filtered by payment mode.
     * Used by: GET /api/orders/by-payment-mode/{mode}
     */
    List<Order> findByPaymentMode(PaymentMode paymentMode);

    /**
     * Orders placed within an inclusive date range.
     * Used by: GET /api/orders/by-date-range?from=&to=
     */
    List<Order> findByOrderDateBetween(LocalDate from, LocalDate to);

    /**
     * Paginated list of all orders, optionally filtered by a date range.
     * Used by: GET /api/orders?from=2023-10-01&to=2023-10-31
     */
    Page<Order> findByOrderDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    // ---------------------------------------------------------------
    // Custom JPQL queries
    // ---------------------------------------------------------------

    /**
     * Total revenue (sum of total_amount) within an inclusive date range.
     * Used by: GET /api/reports/revenue?from=&to=
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o WHERE o.orderDate BETWEEN :from AND :to")
    BigDecimal calculateRevenueBetween(@Param("from") LocalDate from,
                                       @Param("to") LocalDate to);

    /**
     * Count of orders within an inclusive date range.
     * Used alongside revenue calculation for the revenue report.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :from AND :to")
    long countOrdersBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * Revenue and order count grouped by payment mode.
     * Used by: GET /api/reports/revenue-by-payment-mode
     *
     * @return list of Object arrays where:
     *         [0] = PaymentMode,
     *         [1] = BigDecimal totalRevenue,
     *         [2] = Long orderCount
     */
    @Query("SELECT o.paymentMode, COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
           "FROM Order o GROUP BY o.paymentMode")
    List<Object[]> findRevenueByPaymentMode();

    /**
     * Customer ranked by number of orders, descending.
     * Used by: GET /api/reports/customer-with-most-orders
     *
     * @return list of Object arrays where:
     *         [0] = Customer entity,
     *         [1] = Long orderCount
     */
    @Query("SELECT o.customer, COUNT(o) AS orderCount " +
           "FROM Order o GROUP BY o.customer ORDER BY orderCount DESC")
    List<Object[]> findCustomerRankedByOrderCount(Pageable pageable);
}
