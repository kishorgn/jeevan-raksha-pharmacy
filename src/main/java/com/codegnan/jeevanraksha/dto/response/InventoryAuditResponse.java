package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for the inventory audit report.
 *
 * <p>Used by: GET /api/reports/inventory-audit</p>
 *
 * <p>Combines medicine stock information with supplier contact details,
 * enabling pharmacy staff to place reorders directly from the audit sheet.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAuditResponse {

    // --- Medicine Info ---
    private Integer medicineId;
    private String medicineName;
    private String category;
    private Integer stockQuantity;
    private LocalDate expiryDate;

    // --- Supplier Info ---
    private Integer supplierId;
    private String supplierName;
    private String supplierContact;
    private String supplierPhone;
}
