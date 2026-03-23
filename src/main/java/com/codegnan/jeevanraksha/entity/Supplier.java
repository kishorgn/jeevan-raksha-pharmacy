package com.codegnan.jeevanraksha.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a medicine supplier / distributor.
 *
 * <p>Maps to the {@code suppliers} table in the
 * {@code jeevan_raksha_pharmacy} database.</p>
 *
 * <p>A supplier can supply multiple medicines; the relationship is
 * one-to-many with the {@link Medicine} entity.</p>
 */
@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Integer supplierId;

    /** Registered business name of the supplier. */
    @Column(name = "supplier_name", length = 100)
    private String supplierName;

    /** Name of the primary contact person at the supplier. */
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    /** Contact phone number for the supplier. */
    @Column(name = "phone", length = 15)
    private String phone;

    /**
     * All medicines provided by this supplier.
     * Lazily loaded; fetched explicitly in service layer when required.
     */
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Medicine> medicines = new ArrayList<>();
}
