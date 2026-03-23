package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for a supplier's full profile.
 *
 * <p>Used by: GET /api/suppliers/{supplierId}</p>
 *
 * <p>Includes supplier details, their full medicine catalogue,
 * and an aggregated average price across all supplied medicines.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDetailResponse {

    private Integer supplierId;
    private String supplierName;
    private String contactPerson;
    private String phone;

    /** Total number of distinct medicines supplied. */
    private long medicineCount;

    /** Average retail price across all supplied medicines (INR). */
    private BigDecimal averagePrice;

    /** Full list of medicines supplied by this supplier. */
    private List<MedicineResponse> medicines;
}
