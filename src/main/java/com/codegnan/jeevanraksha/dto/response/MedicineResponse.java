package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for a medicine's full details.
 *
 * <p>Used across medicine, inventory, and report endpoints.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineResponse {

    private Integer medicineId;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
    private LocalDate expiryDate;

    /** ID of the supplier who provides this medicine. */
    private Integer supplierId;

    /** Business name of the supplier. */
    private String supplierName;
}
