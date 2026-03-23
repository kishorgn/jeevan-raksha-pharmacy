package com.codegnan.jeevanraksha.entity;

import com.codegnan.jeevanraksha.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a pharmacy sales order (bill / transaction).
 *
 * <p>Maps to the {@code orders} table in the
 * {@code jeevan_raksha_pharmacy} database.</p>
 *
 * <p>An order is placed by one {@link Customer} and contains one or
 * more {@link OrderItem} line items referencing specific medicines.</p>
 *
 * <p><strong>Note:</strong> The table name is {@code orders}; although
 * {@code ORDER} is a reserved SQL keyword, {@code orders} (plural) is
 * safe to use as a table name in MySQL.</p>
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    /**
     * Customer who placed this order.
     * Fetched lazily; joined explicitly when customer info is needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    /** Date on which the order was placed. Set to today when creating. */
    @Column(name = "order_date")
    private LocalDate orderDate;

    /** Total monetary value of all items in this order (INR). */
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Payment method used for this transaction.
     * Stored as a string (e.g., "UPI", "Cash", "Card") to match the
     * MySQL ENUM column.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode")
    private PaymentMode paymentMode;

    /**
     * Individual line items in this order.
     * Cascade ALL ensures items are persisted/deleted with the order.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}
