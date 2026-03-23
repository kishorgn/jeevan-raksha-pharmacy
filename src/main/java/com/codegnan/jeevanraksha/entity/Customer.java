package com.codegnan.jeevanraksha.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a pharmacy customer.
 *
 * <p>Maps to the {@code customers} table in the
 * {@code jeevan_raksha_pharmacy} database.</p>
 *
 * <p>A customer may place multiple orders; the relationship is
 * one-to-many with the {@link Order} entity.</p>
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    /** Full name of the customer. */
    @Column(name = "name", length = 100)
    private String name;

    /** 10–15 digit contact phone number. */
    @Column(name = "phone", length = 15)
    private String phone;

    /** City where the customer resides. */
    @Column(name = "city", length = 50)
    private String city;

    /**
     * All orders placed by this customer.
     * Lazily loaded to avoid N+1 issues; explicitly fetched when needed.
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
