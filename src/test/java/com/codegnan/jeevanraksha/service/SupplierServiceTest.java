package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.SupplierRequest;
import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.dto.response.SupplierDetailResponse;
import com.codegnan.jeevanraksha.dto.response.SupplierResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SupplierService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierService Unit Tests")
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private SupplierService supplierService;

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

    private SupplierRequest buildRequest() {
        SupplierRequest req = new SupplierRequest();
        req.setSupplierName("Apollo Distributors");
        req.setContactPerson("Rajesh Gupta");
        req.setPhone("022-123456");
        return req;
    }

    // ── getAllSuppliers ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllSuppliers: returns all suppliers with their medicine counts")
    void getAllSuppliers_returnsListWithMedicineCounts() {
        // Arrange
        Supplier s = buildSupplier();
        Object[] row = new Object[]{s, 3L};
        List<Object[]> supplierRows = new ArrayList<>();
        supplierRows.add(row);
        when(supplierRepository.findAllSuppliersWithMedicineCount()).thenReturn(supplierRows);

        // Act
        List<SupplierResponse> result = supplierService.getAllSuppliers();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSupplierName()).isEqualTo("Apollo Distributors");
        assertThat(result.get(0).getMedicineCount()).isEqualTo(3L);
    }

    // ── getSupplierById ───────────────────────────────────────────────────

    @Test
    @DisplayName("getSupplierById: existing ID → returns detail with medicines and average price")
    void getSupplierById_whenExists_returnsDetailResponse() {
        // Arrange
        Supplier supplier = buildSupplier();
        Medicine med = buildMedicine(supplier);

        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(medicineRepository.findBySupplierSupplierId(1)).thenReturn(List.of(med));
        when(supplierRepository.findAvgPriceBySupplierId(1)).thenReturn(new BigDecimal("30.00"));

        // Act
        SupplierDetailResponse result = supplierService.getSupplierById(1);

        // Assert
        assertThat(result.getSupplierId()).isEqualTo(1);
        assertThat(result.getSupplierName()).isEqualTo("Apollo Distributors");
        assertThat(result.getMedicineCount()).isEqualTo(1L);
        assertThat(result.getAveragePrice()).isEqualByComparingTo("30.00");
        assertThat(result.getMedicines()).hasSize(1);
        assertThat(result.getMedicines().get(0).getName()).isEqualTo("Dolo 650");
    }

    @Test
    @DisplayName("getSupplierById: non-existent ID → throws ResourceNotFoundException")
    void getSupplierById_whenNotExists_throwsResourceNotFoundException() {
        // Arrange
        when(supplierRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> supplierService.getSupplierById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier")
                .hasMessageContaining("99");
    }

    // ── createSupplier ────────────────────────────────────────────────────

    @Test
    @DisplayName("createSupplier: valid request → saves and returns SupplierResponse")
    void createSupplier_validRequest_returnsCreatedSupplier() {
        // Arrange
        Supplier saved = buildSupplier();
        when(supplierRepository.save(any(Supplier.class))).thenReturn(saved);

        // Act
        SupplierResponse result = supplierService.createSupplier(buildRequest());

        // Assert
        assertThat(result.getSupplierName()).isEqualTo("Apollo Distributors");
        assertThat(result.getMedicineCount()).isEqualTo(0L);
        verify(supplierRepository).save(any(Supplier.class));
    }

    // ── updateSupplier ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateSupplier: existing ID → updates fields and returns updated response")
    void updateSupplier_whenExists_returnsUpdatedSupplier() {
        // Arrange
        Supplier existing = buildSupplier();
        when(supplierRepository.findById(1)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(existing)).thenReturn(existing);
        when(supplierRepository.countMedicinesBySupplierId(1)).thenReturn(2L);

        SupplierRequest updateReq = new SupplierRequest();
        updateReq.setSupplierName("Apollo Healthcare");
        updateReq.setContactPerson("New Contact");
        updateReq.setPhone("022-999999");

        // Act
        SupplierResponse result = supplierService.updateSupplier(1, updateReq);

        // Assert
        assertThat(result.getSupplierName()).isEqualTo("Apollo Healthcare");
        assertThat(result.getMedicineCount()).isEqualTo(2L);
    }

    // ── deleteSupplier ────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteSupplier: no medicines linked → deletes successfully")
    void deleteSupplier_whenNoMedicines_deletesSuccessfully() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.of(buildSupplier()));
        when(supplierRepository.countMedicinesBySupplierId(1)).thenReturn(0L);

        // Act
        supplierService.deleteSupplier(1);

        // Assert
        verify(supplierRepository).deleteById(1);
    }

    @Test
    @DisplayName("deleteSupplier: medicines are still linked → throws ResourceConstraintException")
    void deleteSupplier_whenHasMedicines_throwsResourceConstraintException() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.of(buildSupplier()));
        when(supplierRepository.countMedicinesBySupplierId(1)).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> supplierService.deleteSupplier(1))
                .isInstanceOf(ResourceConstraintException.class)
                .hasMessageContaining("5 medicine(s)");

        verify(supplierRepository, never()).deleteById(any());
    }

    // ── getMedicinesBySupplier ────────────────────────────────────────────

    @Test
    @DisplayName("getMedicinesBySupplier: existing supplier → returns list of MedicineResponse")
    void getMedicinesBySupplier_whenExists_returnsMedicines() {
        // Arrange
        Supplier supplier = buildSupplier();
        Medicine med = buildMedicine(supplier);

        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(medicineRepository.findBySupplierSupplierId(1)).thenReturn(List.of(med));

        // Act
        List<MedicineResponse> result = supplierService.getMedicinesBySupplier(1);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Dolo 650");
        assertThat(result.get(0).getSupplierName()).isEqualTo("Apollo Distributors");
    }

    @Test
    @DisplayName("getMedicinesBySupplier: non-existent supplier → throws ResourceNotFoundException")
    void getMedicinesBySupplier_whenSupplierNotFound_throwsResourceNotFoundException() {
        // Arrange
        when(supplierRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> supplierService.getMedicinesBySupplier(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
