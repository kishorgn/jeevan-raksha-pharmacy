package com.codegnan.jeevanraksha.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response envelope used by every REST endpoint in the application.
 *
 * <p>All API responses are wrapped in this envelope to provide a uniform
 * contract to API consumers, regardless of whether the call succeeded or
 * failed.</p>
 *
 * <p><strong>Success example:</strong>
 * <pre>
 * {
 *   "status": "success",
 *   "message": "Customer retrieved successfully",
 *   "data": { ... }
 * }
 * </pre>
 * </p>
 *
 * <p><strong>Error example (produced by {@link com.codegnan.jeevanraksha.exception.GlobalExceptionHandler}):</strong>
 * <pre>
 * {
 *   "status": "error",
 *   "message": "Customer with id 99 not found",
 *   "data": null
 * }
 * </pre>
 * </p>
 *
 * @param <T> the type of the payload held in {@code data}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** "success" for 2xx responses, "error" for 4xx / 5xx responses. */
    private String status;

    /** Human-readable description of the outcome. */
    private String message;

    /** Payload: a DTO, a list of DTOs, or {@code null} on error. */
    private T data;

    // ---------------------------------------------------------------
    // Static factory helpers — keeps controller code concise
    // ---------------------------------------------------------------

    /**
     * Creates a success envelope with a payload.
     *
     * @param message descriptive success message
     * @param data    the response payload
     * @param <T>     type of the payload
     * @return populated success envelope
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a success envelope without a payload (e.g., delete operations).
     *
     * @param message descriptive success message
     * @param <T>     type parameter (usually {@code Void})
     * @return populated success envelope with null data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .build();
    }

    /**
     * Creates an error envelope.
     *
     * @param message descriptive error message
     * @param <T>     type parameter
     * @return populated error envelope with null data
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .build();
    }
}
