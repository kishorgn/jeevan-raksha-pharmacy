package com.codegnan.jeevanraksha.entity;

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
 * JPA entity representing a medicine in the pharmacy's inventory.
 *
 * <p>Maps to the {@code medicines} table in the
 * {@code jeevan_raksha_pharmacy} database.</p>
 *
 * <p>Each medicine belongs to exactly one {@link Supplier} and can
 * appear in multiple {@link OrderItem} records.</p>
 */
@Entity
@Table(name = "medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Integer medicineId;

    /** Commercial name of the medicine (e.g., "Dolo 650"). */
    @Column(name = "name", length = 100)
    private String name;

    /** Category of the medicine (e.g., "Tablet", "Syrup", "Injection"). */
    @Column(name = "category", length = 50)
    private String category;

    /** Retail price per unit in INR. */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /** Current available stock quantity in units. */
    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    /** Date after which the medicine must not be dispensed. */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * Supplier from whom this medicine is procured.
     * Lazily loaded to avoid unnecessary joins.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    /**
     * All order line items referencing this medicine.
     * Lazily loaded; used only for reporting queries.
     */
    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}
