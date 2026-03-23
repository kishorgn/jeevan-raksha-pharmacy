package com.codegnan.jeevanraksha.controller;

import com.codegnan.jeevanraksha.dto.response.ApiResponse;
import com.codegnan.jeevanraksha.dto.response.SearchResponse;
import com.codegnan.jeevanraksha.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for cross-entity full-text search.
 *
 * <p>Base path: {@code /api/search}</p>
 *
 * <p>Exposes a single endpoint that searches across medicines
 * (by name) and suppliers (by supplier name) based on the
 * query term and type filter.</p>
 */
@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Cross-entity full-text search across medicines and suppliers")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // ---------------------------------------------------------------
    // GET /api/search
    // ---------------------------------------------------------------

    /**
     * Cross-entity full-text search across medicines and suppliers.
     *
     * <p>Samples:
     * <ul>
     *   <li>{@code GET /api/search?q=dolo&type=all} — search both</li>
     *   <li>{@code GET /api/search?q=dolo&type=medicine} — only medicines</li>
     *   <li>{@code GET /api/search?q=apollo&type=supplier} — only suppliers</li>
     * </ul>
     * </p>
     *
     * @param q    the search keyword (case-insensitive contains match)
     * @param type entity scope: "all" (default), "medicine", or "supplier"
     */
    @GetMapping
    @Operation(summary = "Search medicines and suppliers",
               description = "Case-insensitive full-text search. " +
                             "Use type=all (default), type=medicine, or type=supplier to scope results.")
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @Parameter(description = "Search keyword", example = "dolo", required = true)
            @RequestParam String q,

            @Parameter(description = "Scope filter: all | medicine | supplier", example = "all")
            @RequestParam(defaultValue = "all") String type) {

        logger.info("GET /api/search | q='{}' type='{}'", q, type);
        SearchResponse result = searchService.search(q, type);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Search complete — %d result(s) found", result.getTotalResults()),
                result));
    }
}
