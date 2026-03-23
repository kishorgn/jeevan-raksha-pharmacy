package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.dto.response.SearchResponse;
import com.codegnan.jeevanraksha.dto.response.SupplierResponse;
import com.codegnan.jeevanraksha.entity.Medicine;
import com.codegnan.jeevanraksha.entity.Supplier;
import com.codegnan.jeevanraksha.exception.InvalidRequestException;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import com.codegnan.jeevanraksha.repository.SupplierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SearchService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchService Unit Tests")
class SearchServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private MedicineService medicineService;

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private SearchService searchService;

    // ── Test data helpers ─────────────────────────────────────────────────

    private Supplier buildSupplier() {
        Supplier s = new Supplier();
        s.setSupplierId(1);
        s.setSupplierName("Apollo Distributors");
        s.setContactPerson("Rajesh Gupta");
        s.setPhone("022-123456");
        return s;
    }

    private Medicine buildMedicine(Supplier supplier) {
        Medicine m = new Medicine();
        m.setMedicineId(1);
        m.setName("Dolo 650");
        m.setCategory("Tablet");
        m.setPrice(new BigDecimal("30.00"));
        m.setStockQuantity(500);
        m.setExpiryDate(LocalDate.now().plusMonths(12));
        m.setSupplier(supplier);
        return m;
    }

    private MedicineResponse buildMedicineResponse() {
        return MedicineResponse.builder()
                .medicineId(1)
                .name("Dolo 650")
                .category("Tablet")
                .price(new BigDecimal("30.00"))
                .stockQuantity(500)
                .supplierId(1)
                .supplierName("Apollo Distributors")
                .build();
    }

    private SupplierResponse buildSupplierResponse() {
        return SupplierResponse.builder()
                .supplierId(1)
                .supplierName("Apollo Distributors")
                .contactPerson("Rajesh Gupta")
                .phone("022-123456")
                .medicineCount(3L)
                .build();
    }

    // ── search (type = "all") ─────────────────────────────────────────────

    @Test
    @DisplayName("search: type='all' → returns both matching medicines and suppliers")
    void search_whenTypeAll_returnsMedicinesAndSuppliers() {
        // Arrange
        Supplier supplier = buildSupplier();
        Medicine medicine = buildMedicine(supplier);

        when(medicineRepository.findByNameContainingIgnoreCase("dolo"))
                .thenReturn(List.of(medicine));
        when(medicineService.toResponse(medicine)).thenReturn(buildMedicineResponse());

        when(supplierRepository.searchByName("dolo")).thenReturn(List.of(supplier));
        when(supplierRepository.countMedicinesBySupplierId(1)).thenReturn(3L);
        when(supplierService.toResponseWithCount(supplier, 3L)).thenReturn(buildSupplierResponse());

        // Act
        SearchResponse result = searchService.search("dolo", "all");

        // Assert
        assertThat(result.getQuery()).isEqualTo("dolo");
        assertThat(result.getType()).isEqualTo("all");
        assertThat(result.getMedicines()).hasSize(1);
        assertThat(result.getSuppliers()).hasSize(1);
        assertThat(result.getTotalResults()).isEqualTo(2);
        assertThat(result.getMedicines().get(0).getName()).isEqualTo("Dolo 650");
        assertThat(result.getSuppliers().get(0).getSupplierName()).isEqualTo("Apollo Distributors");
    }

    // ── search (type = "medicine") ────────────────────────────────────────

    @Test
    @DisplayName("search: type='medicine' → returns only medicines, skips supplier repository")
    void search_whenTypeMedicine_returnsMedicinesOnly() {
        // Arrange
        Supplier supplier = buildSupplier();
        Medicine medicine = buildMedicine(supplier);

        when(medicineRepository.findByNameContainingIgnoreCase("dolo"))
                .thenReturn(List.of(medicine));
        when(medicineService.toResponse(medicine)).thenReturn(buildMedicineResponse());

        // Act
        SearchResponse result = searchService.search("dolo", "medicine");

        // Assert
        assertThat(result.getType()).isEqualTo("medicine");
        assertThat(result.getMedicines()).hasSize(1);
        assertThat(result.getSuppliers()).isEmpty();
        assertThat(result.getTotalResults()).isEqualTo(1);

        // Supplier repository must NOT be called
        verify(supplierRepository, never()).searchByName(any());
    }

    // ── search (type = "supplier") ────────────────────────────────────────

    @Test
    @DisplayName("search: type='supplier' → returns only suppliers, skips medicine repository")
    void search_whenTypeSupplier_returnsSuppliersOnly() {
        // Arrange
        Supplier supplier = buildSupplier();

        when(supplierRepository.searchByName("apollo")).thenReturn(List.of(supplier));
        when(supplierRepository.countMedicinesBySupplierId(1)).thenReturn(3L);
        when(supplierService.toResponseWithCount(supplier, 3L)).thenReturn(buildSupplierResponse());

        // Act
        SearchResponse result = searchService.search("apollo", "supplier");

        // Assert
        assertThat(result.getType()).isEqualTo("supplier");
        assertThat(result.getSuppliers()).hasSize(1);
        assertThat(result.getMedicines()).isEmpty();
        assertThat(result.getTotalResults()).isEqualTo(1);

        // Medicine repository must NOT be called
        verify(medicineRepository, never()).findByNameContainingIgnoreCase(any());
    }

    // ── search: null / blank type defaults to "all" ───────────────────────

    @Test
    @DisplayName("search: null type → defaults to 'all' and queries both repositories")
    void search_whenTypeNull_defaultsToAll() {
        // Arrange
        Supplier supplier = buildSupplier();
        Medicine medicine = buildMedicine(supplier);

        when(medicineRepository.findByNameContainingIgnoreCase("dolo"))
                .thenReturn(List.of(medicine));
        when(medicineService.toResponse(medicine)).thenReturn(buildMedicineResponse());
        when(supplierRepository.searchByName("dolo")).thenReturn(List.of());

        // Act
        SearchResponse result = searchService.search("dolo", null);

        // Assert — type resolved to "all"
        assertThat(result.getType()).isEqualTo("all");
        assertThat(result.getMedicines()).hasSize(1);
        assertThat(result.getSuppliers()).isEmpty();
        assertThat(result.getTotalResults()).isEqualTo(1);
    }

    // ── search: no results ────────────────────────────────────────────────

    @Test
    @DisplayName("search: no matches found → returns empty lists with totalResults = 0")
    void search_whenNoMatches_returnsEmptyResult() {
        // Arrange
        when(medicineRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(List.of());
        when(supplierRepository.searchByName("xyz")).thenReturn(List.of());

        // Act
        SearchResponse result = searchService.search("xyz", "all");

        // Assert
        assertThat(result.getMedicines()).isEmpty();
        assertThat(result.getSuppliers()).isEmpty();
        assertThat(result.getTotalResults()).isEqualTo(0);
    }

    // ── search: invalid inputs → InvalidRequestException ─────────────────

    @Test
    @DisplayName("search: blank query → throws InvalidRequestException")
    void search_whenQueryBlank_throwsInvalidRequestException() {
        // Act & Assert
        assertThatThrownBy(() -> searchService.search("  ", "all"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("must not be empty");
    }

    @Test
    @DisplayName("search: null query → throws InvalidRequestException")
    void search_whenQueryNull_throwsInvalidRequestException() {
        // Act & Assert
        assertThatThrownBy(() -> searchService.search(null, "all"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("must not be empty");
    }

    @Test
    @DisplayName("search: invalid type value → throws InvalidRequestException")
    void search_whenTypeInvalid_throwsInvalidRequestException() {
        // Act & Assert
        assertThatThrownBy(() -> searchService.search("dolo", "unknown"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid search type")
                .hasMessageContaining("unknown");
    }

    // ── search: type is case-insensitive ──────────────────────────────────

    @Test
    @DisplayName("search: type 'MEDICINE' (uppercase) → normalised and executed correctly")
    void search_whenTypeUpperCase_normalisedAndExecuted() {
        // Arrange
        Supplier supplier = buildSupplier();
        Medicine medicine = buildMedicine(supplier);

        when(medicineRepository.findByNameContainingIgnoreCase("dolo"))
                .thenReturn(List.of(medicine));
        when(medicineService.toResponse(medicine)).thenReturn(buildMedicineResponse());

        // Act
        SearchResponse result = searchService.search("dolo", "MEDICINE");

        // Assert — type normalised to lower-case
        assertThat(result.getType()).isEqualTo("medicine");
        assertThat(result.getMedicines()).hasSize(1);
        verify(supplierRepository, never()).searchByName(any());
    }
}
