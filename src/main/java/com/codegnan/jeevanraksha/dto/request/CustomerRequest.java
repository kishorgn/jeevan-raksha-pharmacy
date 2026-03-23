package com.codegnan.jeevanraksha.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or updating a {@link com.codegnan.jeevanraksha.entity.Customer}.
 *
 * <p>All fields are validated using Jakarta Bean Validation annotations.
 * Invalid requests are rejected by the framework before reaching the
 * service layer, and the errors are surfaced through
 * {@link com.codegnan.jeevanraksha.exception.GlobalExceptionHandler}.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    /** Full name of the customer. Must not be blank, max 100 characters. */
    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    /**
     * Contact phone number.
     * Accepts 10–15 digit numeric strings, optionally starting with '+'.
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be 10–15 digits")
    private String phone;

    /** City of residence. Must not be blank, max 50 characters. */
    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;
}
