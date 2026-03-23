package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for basic customer information.
 *
 * <p>Used in list endpoints and wherever only the core customer profile
 * fields are needed (e.g., GET /api/customers).</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Integer customerId;
    private String name;
    private String phone;
    private String city;
}
