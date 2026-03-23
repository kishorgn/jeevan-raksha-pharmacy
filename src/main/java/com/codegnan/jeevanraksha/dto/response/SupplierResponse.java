package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for basic supplier information, including the medicine count.
 *
 * <p>Used by: GET /api/suppliers (list all suppliers).</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierResponse {

    private Integer supplierId;
    private String supplierName;
    private String contactPerson;
    private String phone;

    /** Total number of distinct medicines supplied by this supplier. */
    private long medicineCount;
}
