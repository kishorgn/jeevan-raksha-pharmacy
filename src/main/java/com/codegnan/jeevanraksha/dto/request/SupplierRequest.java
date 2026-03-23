package com.codegnan.jeevanraksha.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or updating a {@link com.codegnan.jeevanraksha.entity.Supplier}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequest {

    /** Registered business name of the supplier. */
    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name must not exceed 100 characters")
    private String supplierName;

    /** Name of the primary contact person at the supplier. */
    @NotBlank(message = "Contact person name is required")
    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    private String contactPerson;

    /** Phone number of the supplier (landline or mobile). */
    @NotBlank(message = "Phone number is required")
    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;
}
