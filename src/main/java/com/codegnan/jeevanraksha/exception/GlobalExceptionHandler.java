package com.codegnan.jeevanraksha.exception;

import com.codegnan.jeevanraksha.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for the entire application.
 *
 * <p>Annotated with {@code @RestControllerAdvice}, which means it intercepts
 * exceptions thrown from any {@code @RestController} and converts them into
 * consistent {@link ApiResponse} error envelopes before they reach the client.</p>
 *
 * <p>This eliminates duplicated try-catch blocks in controllers and ensures
 * all error responses follow the same JSON structure.</p>
 *
 * <p><strong>HTTP Status Mapping:</strong>
 * <ul>
 *   <li>404 — {@link ResourceNotFoundException}</li>
 *   <li>409 — {@link ResourceConstraintException}</li>
 *   <li>422 — {@link InsufficientStockException}</li>
 *   <li>400 — {@link InvalidRequestException}, validation errors, malformed JSON</li>
 *   <li>500 — all uncaught exceptions</li>
 * </ul>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------------------------------------------
    // 1. Domain-specific exceptions
    // ---------------------------------------------------------------

    /**
     * Handles 404 when a requested entity is not found in the database.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles 409 when a delete is blocked by referential integrity.
     */
    @ExceptionHandler(ResourceConstraintException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceConstraint(ResourceConstraintException ex) {
        logger.warn("Resource constraint violation: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles 422 when requested stock quantity exceeds available stock.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        logger.warn("Insufficient stock: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles 400 for business-rule validation failures in the service layer.
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(InvalidRequestException ex) {
        logger.warn("Invalid request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ---------------------------------------------------------------
    // 2. Spring / Jakarta validation exceptions
    // ---------------------------------------------------------------

    /**
     * Handles 400 for {@code @Valid} annotation failures on request bodies.
     *
     * <p>Returns a map of field names to their specific error messages,
     * making it easy for API consumers to highlight the offending fields.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        logger.warn("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .status("error")
                        .message("Validation failed. Please check the request fields.")
                        .data(errors)
                        .build());
    }

    /**
     * Handles 400 for missing required query parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex) {
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        logger.warn("Missing request parameter: {}", ex.getParameterName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * Handles 400 for path/query variables with wrong type (e.g., string instead of int).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "Parameter '%s' has invalid value '%s'. Expected type: %s",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        logger.warn("Type mismatch: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * Handles 400 for malformed JSON request bodies (e.g., invalid enum value,
     * wrong data type).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(
            HttpMessageNotReadableException ex) {
        logger.warn("Unreadable request body: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Malformed JSON request body. Please check the format and field types."));
    }

    // ---------------------------------------------------------------
    // 3. Catch-all fallback
    // ---------------------------------------------------------------

    /**
     * Handles 500 for any unhandled exception.
     *
     * <p>Logs the full stack trace internally but returns a generic message
     * to the client to avoid leaking implementation details.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}
