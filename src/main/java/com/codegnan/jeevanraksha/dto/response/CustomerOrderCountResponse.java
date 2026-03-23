package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO identifying the customer who placed the most orders.
 *
 * <p>Used by: GET /api/reports/customer-with-most-orders</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderCountResponse {

    private Integer customerId;
    private String name;
    private String city;
    private String phone;

    /** Total number of orders placed by this customer. */
    private long orderCount;
}
