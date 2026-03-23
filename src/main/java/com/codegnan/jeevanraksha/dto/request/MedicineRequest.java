package com.codegnan.jeevanraksha.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for creating or updating a {@link com.codegnan.jeevanraksha.entity.Medicine}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineRequest {

    /** Commercial name of the medicine (e.g., "Dolo 650"). */
    @NotBlank(message = "Medicine name is required")
    @Size(max = 100, message = "Medicine name must not exceed 100 characters")
    private String name;

    /** Category (e.g., "Tablet", "Syrup", "Injection"). */
    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    /** Retail price per unit in INR. Must be greater than 0. */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    /** Initial stock quantity. Must be zero or more. */
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be zero or more")
    private Integer stockQuantity;

    /** Expiry date of the medicine. Must be a future or present date. */
    @NotNull(message = "Expiry date is required")
    @FutureOrPresent(message = "Expiry date must be today or in the future")
    private LocalDate expiryDate;

    /** ID of the supplier who provides this medicine. */
    @NotNull(message = "Supplier ID is required")
    @Positive(message = "Supplier ID must be a positive integer")
    private Integer supplierId;
}
