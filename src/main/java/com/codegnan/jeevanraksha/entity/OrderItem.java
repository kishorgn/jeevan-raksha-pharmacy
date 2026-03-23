package com.codegnan.jeevanraksha.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA entity representing a single line item within a pharmacy order.
 *
 * <p>Maps to the {@code order_items} table in the
 * {@code jeevan_raksha_pharmacy} database.</p>
 *
 * <p>Each record links one {@link Order} to one {@link Medicine},
 * recording the quantity purchased and the computed subtotal
 * ({@code price × quantity}) at the time of the order.</p>
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    /**
     * Parent order that contains this line item.
     * Lazily loaded; always accessible through the Order entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * Medicine being purchased in this line item.
     * Lazily loaded; joined explicitly for detailed responses.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    /** Number of units of the medicine purchased. Must be >= 1. */
    @Column(name = "quantity")
    private Integer quantity;

    /**
     * Computed subtotal for this line item.
     * Calculated as {@code medicine.price × quantity} at order time.
     * Stored to preserve historical pricing even if medicine price changes.
     */
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;
}
