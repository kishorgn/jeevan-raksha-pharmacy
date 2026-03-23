package com.codegnan.jeevanraksha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for the cross-entity search endpoint.
 *
 * <p>Used by: GET /api/search?q=dolo&type=all</p>
 *
 * <p>Aggregates results from medicines and suppliers into a single response.
 * Depending on the {@code type} query parameter, only one or both lists
 * may be populated.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponse {

    /** Search query term that was used. */
    private String query;

    /** Search type filter used: "all", "medicine", or "supplier". */
    private String type;

    /** Matching medicines (populated when type is "all" or "medicine"). */
    private List<MedicineResponse> medicines;

    /** Matching suppliers (populated when type is "all" or "supplier"). */
    private List<SupplierResponse> suppliers;

    /** Total count of all results across both categories. */
    private int totalResults;
}
