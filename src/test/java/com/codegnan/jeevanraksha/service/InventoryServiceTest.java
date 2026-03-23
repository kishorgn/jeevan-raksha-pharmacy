package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.RestockRequest;
import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.entity.Medicine;
import com.codegnan.jeevanraksha.entity.Supplier;
import com.codegnan.jeevanraksha.exception.ResourceNotFoundException;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link InventoryService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private MedicineService medicineService;

    @InjectMocks
    private InventoryService inventoryService;

    // ── Test data helpers ─────────────────────────────────────────────────

    private Medicine buildMedicine(int stock) {
        Supplier s = new Supplier();
        s.setSupplierId(1);
        s.setSupplierName("Apollo Distributors");
        s.setContactPerson("Rajesh Gupta");
        s.setPhone("022-123456");

        Medicine m = new Medicine();
        m.setMedicineId(1);
        m.setName("Dolo 650");
        m.setCategory("Tablet");
        m.setPrice(new BigDecimal("30.00"));
        m.setStockQuantity(stock);
        m.setExpiryDate(LocalDate.now().plusMonths(12));
        m.setSupplier(s);
        return m;
    }

    private MedicineResponse buildMedicineResponse(int stock) {
        return MedicineResponse.builder()
                .medicineId(1)
                .name("Dolo 650")
                .category("Tablet")
                .price(new BigDecimal("30.00"))
                .stockQuantity(stock)
                .expiryDate(LocalDate.now().plusMonths(12))
                .supplierId(1)
                .supplierName("Apollo Distributors")
                .build();
    }

    // ── getAllInventory ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAllInventory: returns full stock list for all medicines")
    void getAllInventory_returnsAllMedicines() {
        // Arrange
        Medicine med = buildMedicine(500);
        when(medicineRepository.findAll()).thenReturn(List.of(med));
        when(medicineService.toResponse(med)).thenReturn(buildMedicineResponse(500));

        // Act
        List<MedicineResponse> result = inventoryService.getAllInventory();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStockQuantity()).isEqualTo(500);
        verify(medicineRepository).findAll();
    }

    // ── getLowStockMedicines ──────────────────────────────────────────────

    @Test
    @DisplayName("getLowStockMedicines: threshold provided → returns medicines at or below threshold")
    void getLowStockMedicines_whenThresholdProvided_returnsFilteredList() {
        // Arrange
        Medicine med = buildMedicine(20);
        when(medicineRepository.findByStockQuantityLessThanEqual(50)).thenReturn(List.of(med));
        when(medicineService.toResponse(med)).thenReturn(buildMedicineResponse(20));

        // Act
        List<MedicineResponse> result = inventoryService.getLowStockMedicines(50);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStockQuantity()).isEqualTo(20);
        verify(medicineRepository).findByStockQuantityLessThanEqual(50);
    }

    @Test
    @DisplayName("getLowStockMedicines: null threshold → uses default value of 50")
    void getLowStockMedicines_whenNullThreshold_usesDefaultOf50() {
        // Arrange
        Medicine med = buildMedicine(30);
        when(medicineRepository.findByStockQuantityLessThanEqual(50)).thenReturn(List.of(med));
        when(medicineService.toResponse(med)).thenReturn(buildMedicineResponse(30));

        // Act
        List<MedicineResponse> result = inventoryService.getLowStockMedicines(null);

        // Assert
        // Verify that the default threshold (50) is used when null is passed
        verify(medicineRepository).findByStockQuantityLessThanEqual(50);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getLowStockMedicines: no medicines below threshold → returns empty list")
    void getLowStockMedicines_whenNoMedicinesBelow_returnsEmptyList() {
        // Arrange
        when(medicineRepository.findByStockQuantityLessThanEqual(10)).thenReturn(List.of());

        // Act
        List<MedicineResponse> result = inventoryService.getLowStockMedicines(10);

        // Assert
        assertThat(result).isEmpty();
    }

    // ── restockMedicine ───────────────────────────────────────────────────

    @Test
    @DisplayName("restockMedicine: adds delta quantity to existing stock (50 + 100 = 150)")
    void restockMedicine_addsDeltaToCurrentStock() {
        // Arrange
        Medicine medicine = buildMedicine(50);           // current stock = 50
        RestockRequest request = new RestockRequest(100); // add 100 units

        when(medicineRepository.findById(1)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(medicine)).thenReturn(medicine);
        when(medicineService.toResponse(medicine)).thenReturn(buildMedicineResponse(150));

        // Act
        MedicineResponse result = inventoryService.restockMedicine(1, request);

        // Assert — stock should now be 150
        assertThat(medicine.getStockQuantity()).isEqualTo(150);
        verify(medicineRepository).save(medicine);
    }

    @Test
    @DisplayName("restockMedicine: non-existent medicine ID → throws ResourceNotFoundException")
    void restockMedicine_whenMedicineNotFound_throwsResourceNotFoundException() {
        // Arrange
        when(medicineRepository.findById(99)).thenReturn(Optional.empty());

        RestockRequest request = new RestockRequest(50);

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.restockMedicine(99, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medicine")
                .hasMessageContaining("99");

        verify(medicineRepository, never()).save(any(Medicine.class));
    }

    @Test
    @DisplayName("restockMedicine: stock is zero → delta brings it to requested quantity")
    void restockMedicine_whenZeroStock_updatesCorrectly() {
        // Arrange
        Medicine medicine = buildMedicine(0);             // zero stock
        RestockRequest request = new RestockRequest(200); // add 200

        when(medicineRepository.findById(1)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(medicine)).thenReturn(medicine);
        when(medicineService.toResponse(medicine)).thenReturn(buildMedicineResponse(200));

        // Act
        inventoryService.restockMedicine(1, request);

        // Assert
        assertThat(medicine.getStockQuantity()).isEqualTo(200);
    }
}
