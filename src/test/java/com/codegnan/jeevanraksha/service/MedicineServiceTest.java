package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.MedicineRequest;
import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.entity.Medicine;
import com.codegnan.jeevanraksha.entity.Supplier;
import com.codegnan.jeevanraksha.exception.ResourceConstraintException;
import com.codegnan.jeevanraksha.exception.ResourceNotFoundException;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import com.codegnan.jeevanraksha.repository.SupplierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MedicineService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MedicineService Unit Tests")
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private MedicineService medicineService;

    // ── Test data helpers ─────────────────────────────────────────────────

    private Supplier buildSupplier() {
        Supplier s = new Supplier();
        s.setSupplierId(1);
        s.setSupplierName("Apollo Distributors");
        s.setContactPerson("Rajesh Gupta");
        s.setPhone("022-123456");
        return s;
    }

    private Medicine buildMedicine() {
        Supplier supplier = buildSupplier();
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

    private MedicineRequest buildRequest() {
        MedicineRequest req = new MedicineRequest();
        req.setName("Dolo 650");
        req.setCategory("Tablet");
        req.setPrice(new BigDecimal("30.00"));
        req.setStockQuantity(500);
        req.setExpiryDate(LocalDate.now().plusMonths(12));
        req.setSupplierId(1);
        return req;
    }

    // ── getAllMedicines ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAllMedicines: no category filter → returns paginated list of all medicines")
    void getAllMedicines_whenNoCategoryFilter_returnsAllMedicines() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medicine> page = new PageImpl<>(List.of(buildMedicine()));
        when(medicineRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<MedicineResponse> result = medicineService.getAllMedicines(null, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Dolo 650");
        verify(medicineRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getAllMedicines: category filter provided → calls category-filtered query")
    void getAllMedicines_whenCategoryFilter_callsFilteredQuery() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medicine> page = new PageImpl<>(List.of(buildMedicine()));
        when(medicineRepository.findByCategoryIgnoreCase("Tablet", pageable)).thenReturn(page);

        // Act
        Page<MedicineResponse> result = medicineService.getAllMedicines("Tablet", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        verify(medicineRepository).findByCategoryIgnoreCase("Tablet", pageable);
        verify(medicineRepository, never()).findAll(pageable);
    }

    // ── getMedicineById ───────────────────────────────────────────────────

    @Test
    @DisplayName("getMedicineById: existing ID → returns MedicineResponse with supplier info")
    void getMedicineById_whenExists_returnsMedicineResponse() {
        // Arrange
        when(medicineRepository.findById(1)).thenReturn(Optional.of(buildMedicine()));

        // Act
        MedicineResponse result = medicineService.getMedicineById(1);

        // Assert
        assertThat(result.getMedicineId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Dolo 650");
        assertThat(result.getCategory()).isEqualTo("Tablet");
        assertThat(result.getSupplierName()).isEqualTo("Apollo Distributors");
    }

    @Test
    @DisplayName("getMedicineById: non-existent ID → throws ResourceNotFoundException")
    void getMedicineById_whenNotExists_throwsResourceNotFoundException() {
        // Arrange
        when(medicineRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> medicineService.getMedicineById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medicine")
                .hasMessageContaining("99");
    }

    // ── createMedicine ────────────────────────────────────────────────────

    @Test
    @DisplayName("createMedicine: valid request with valid supplier → saves and returns response")
    void createMedicine_validRequest_returnsCreatedMedicine() {
        // Arrange
        Supplier supplier = buildSupplier();
        Medicine saved = buildMedicine();
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(saved);

        // Act
        MedicineResponse result = medicineService.createMedicine(buildRequest());

        // Assert
        assertThat(result.getName()).isEqualTo("Dolo 650");
        assertThat(result.getSupplierName()).isEqualTo("Apollo Distributors");
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    @DisplayName("createMedicine: non-existent supplier ID → throws ResourceNotFoundException")
    void createMedicine_whenSupplierNotFound_throwsResourceNotFoundException() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> medicineService.createMedicine(buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier");
    }

    // ── updateMedicine ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateMedicine: existing ID → updates fields and returns updated response")
    void updateMedicine_whenExists_returnsUpdatedMedicine() {
        // Arrange
        Medicine existing = buildMedicine();
        Supplier supplier = buildSupplier();
        when(medicineRepository.findById(1)).thenReturn(Optional.of(existing));
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(medicineRepository.save(existing)).thenReturn(existing);

        MedicineRequest updateReq = buildRequest();
        updateReq.setPrice(new BigDecimal("35.00"));
        updateReq.setStockQuantity(450);

        // Act
        MedicineResponse result = medicineService.updateMedicine(1, updateReq);

        // Assert
        assertThat(result.getPrice()).isEqualByComparingTo("35.00");
        assertThat(result.getStockQuantity()).isEqualTo(450);
        verify(medicineRepository).save(existing);
    }

    // ── deleteMedicine ────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteMedicine: no order items referencing it → deletes successfully")
    void deleteMedicine_whenNoOrderItems_deletesSuccessfully() {
        // Arrange
        when(medicineRepository.findById(1)).thenReturn(Optional.of(buildMedicine()));
        when(medicineRepository.countOrderItemsByMedicineId(1)).thenReturn(0L);

        // Act
        medicineService.deleteMedicine(1);

        // Assert
        verify(medicineRepository).deleteById(1);
    }

    @Test
    @DisplayName("deleteMedicine: order items reference the medicine → throws ResourceConstraintException")
    void deleteMedicine_whenReferencedInOrders_throwsResourceConstraintException() {
        // Arrange
        when(medicineRepository.findById(1)).thenReturn(Optional.of(buildMedicine()));
        when(medicineRepository.countOrderItemsByMedicineId(1)).thenReturn(2L);

        // Act & Assert
        assertThatThrownBy(() -> medicineService.deleteMedicine(1))
                .isInstanceOf(ResourceConstraintException.class)
                .hasMessageContaining("2 order item(s)");

        verify(medicineRepository, never()).deleteById(any());
    }

    // ── getMedicinesByCategory ────────────────────────────────────────────

    @Test
    @DisplayName("getMedicinesByCategory: returns all medicines matching category")
    void getMedicinesByCategory_returnsMatchingMedicines() {
        // Arrange
        when(medicineRepository.findByCategoryIgnoreCase("Tablet"))
                .thenReturn(List.of(buildMedicine()));

        // Act
        List<MedicineResponse> result = medicineService.getMedicinesByCategory("Tablet");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Tablet");
    }

    // ── getMedicinesByPriceRange ──────────────────────────────────────────

    @Test
    @DisplayName("getMedicinesByPriceRange: returns medicines within min-max price window")
    void getMedicinesByPriceRange_returnsFilteredList() {
        // Arrange
        BigDecimal min = new BigDecimal("20.00");
        BigDecimal max = new BigDecimal("50.00");
        when(medicineRepository.findByPriceBetween(min, max))
                .thenReturn(List.of(buildMedicine()));

        // Act
        List<MedicineResponse> result = medicineService.getMedicinesByPriceRange(min, max);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("30.00");
    }
}
