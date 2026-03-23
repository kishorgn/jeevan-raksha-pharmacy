package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.response.*;
import com.codegnan.jeevanraksha.entity.Customer;
import com.codegnan.jeevanraksha.entity.Medicine;
import com.codegnan.jeevanraksha.entity.Supplier;
import com.codegnan.jeevanraksha.enums.PaymentMode;
import com.codegnan.jeevanraksha.exception.InvalidRequestException;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import com.codegnan.jeevanraksha.repository.OrderItemRepository;
import com.codegnan.jeevanraksha.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReportService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Unit Tests")
class ReportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private MedicineService medicineService;

    @InjectMocks
    private ReportService reportService;

    // ── Test data helpers ─────────────────────────────────────────────────

    private Customer buildCustomer(int id, String name) {
        Customer c = new Customer();
        c.setCustomerId(id);
        c.setName(name);
        c.setCity("Mumbai");
        c.setPhone("9876543210");
        return c;
    }

    private Medicine buildMedicineWithSupplier() {
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
        m.setStockQuantity(20);
        m.setExpiryDate(LocalDate.now().minusDays(5));
        m.setSupplier(s);
        return m;
    }

    // ── getRevenue ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRevenue: valid date range → returns revenue and order count")
    void getRevenue_whenValidRange_returnsRevenueResponse() {
        // Arrange
        LocalDate from = LocalDate.of(2023, 10, 1);
        LocalDate to   = LocalDate.of(2023, 10, 31);

        when(orderRepository.calculateRevenueBetween(from, to)).thenReturn(new BigDecimal("1500.00"));
        when(orderRepository.countOrdersBetween(from, to)).thenReturn(5L);

        // Act
        RevenueResponse result = reportService.getRevenue(from, to);

        // Assert
        assertThat(result.getFrom()).isEqualTo(from);
        assertThat(result.getTo()).isEqualTo(to);
        assertThat(result.getTotalRevenue()).isEqualByComparingTo("1500.00");
        assertThat(result.getOrderCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getRevenue: from date is after to date → throws InvalidRequestException")
    void getRevenue_whenFromAfterTo_throwsInvalidRequestException() {
        // Arrange — invalid range
        LocalDate from = LocalDate.of(2023, 10, 31);
        LocalDate to   = LocalDate.of(2023, 10, 1);

        // Act & Assert
        assertThatThrownBy(() -> reportService.getRevenue(from, to))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("'from' date must not be after 'to' date");
    }

    @Test
    @DisplayName("getRevenue: no orders in range → returns zero revenue")
    void getRevenue_whenNoOrdersInRange_returnsZeroRevenue() {
        // Arrange
        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to   = LocalDate.of(2020, 1, 31);
        when(orderRepository.calculateRevenueBetween(from, to)).thenReturn(BigDecimal.ZERO);
        when(orderRepository.countOrdersBetween(from, to)).thenReturn(0L);

        // Act
        RevenueResponse result = reportService.getRevenue(from, to);

        // Assert
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getOrderCount()).isEqualTo(0L);
    }

    // ── getRevenueByPaymentMode ───────────────────────────────────────────

    @Test
    @DisplayName("getRevenueByPaymentMode: returns one entry per payment mode")
    void getRevenueByPaymentMode_returnsBreakdown() {
        // Arrange — simulate two modes
        Object[] upiRow  = new Object[]{PaymentMode.UPI,  new BigDecimal("1000.00"), 4L};
        Object[] cashRow = new Object[]{PaymentMode.Cash, new BigDecimal("500.00"),  2L};
        List<Object[]> modeRows = new ArrayList<>();
        modeRows.add(upiRow);
        modeRows.add(cashRow);
        when(orderRepository.findRevenueByPaymentMode()).thenReturn(modeRows);

        // Act
        List<RevenueByModeResponse> result = reportService.getRevenueByPaymentMode();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPaymentMode()).isEqualTo(PaymentMode.UPI);
        assertThat(result.get(0).getTotalRevenue()).isEqualByComparingTo("1000.00");
        assertThat(result.get(0).getOrderCount()).isEqualTo(4L);
    }

    // ── getBestsellers ────────────────────────────────────────────────────

    @Test
    @DisplayName("getBestsellers: returns medicines ranked by total quantity sold")
    void getBestsellers_returnsRankedList() {
        // Arrange
        Object[] row = new Object[]{
                1,                            // medicineId
                "Dolo 650",                   // medicineName
                "Tablet",                     // category
                new BigDecimal("30.00"),      // price
                50L,                          // totalQuantitySold
                new BigDecimal("1500.00")     // totalRevenue
        };
        List<Object[]> bestsellerRows = new ArrayList<>();
        bestsellerRows.add(row);
        when(orderItemRepository.findBestsellers()).thenReturn(bestsellerRows);

        // Act
        List<BestsellerResponse> result = reportService.getBestsellers();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMedicineName()).isEqualTo("Dolo 650");
        assertThat(result.get(0).getTotalQuantitySold()).isEqualTo(50L);
        assertThat(result.get(0).getTotalRevenue()).isEqualByComparingTo("1500.00");
    }

    // ── getCustomerWithMostOrders ─────────────────────────────────────────

    @Test
    @DisplayName("getCustomerWithMostOrders: orders exist → returns customer with highest order count")
    void getCustomerWithMostOrders_whenOrdersExist_returnsTopCustomer() {
        // Arrange
        Customer customer = buildCustomer(1, "Sneha Reddy");
        Object[] row = new Object[]{customer, 5L};
        List<Object[]> rankedRows = new ArrayList<>();
        rankedRows.add(row);
        when(orderRepository.findCustomerRankedByOrderCount(any(Pageable.class)))
                .thenReturn(rankedRows);

        // Act
        CustomerOrderCountResponse result = reportService.getCustomerWithMostOrders();

        // Assert
        assertThat(result.getName()).isEqualTo("Sneha Reddy");
        assertThat(result.getOrderCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getCustomerWithMostOrders: no orders exist → throws InvalidRequestException")
    void getCustomerWithMostOrders_whenNoOrders_throwsInvalidRequestException() {
        // Arrange
        when(orderRepository.findCustomerRankedByOrderCount(any(Pageable.class)))
                .thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> reportService.getCustomerWithMostOrders())
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("No orders exist");
    }

    // ── getExpiredMedicines ───────────────────────────────────────────────

    @Test
    @DisplayName("getExpiredMedicines: returns medicines with past expiry dates")
    void getExpiredMedicines_returnsExpiredList() {
        // Arrange
        Medicine expired = buildMedicineWithSupplier(); // expiryDate = 5 days ago
        MedicineResponse response = MedicineResponse.builder()
                .medicineId(1).name("Dolo 650").expiryDate(expired.getExpiryDate()).build();

        when(medicineRepository.findByExpiryDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(expired));
        when(medicineService.toResponse(expired)).thenReturn(response);

        // Act
        List<MedicineResponse> result = reportService.getExpiredMedicines();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Dolo 650");
    }

    @Test
    @DisplayName("getExpiredMedicines: no expired medicines → returns empty list")
    void getExpiredMedicines_whenNoneExpired_returnsEmptyList() {
        // Arrange
        when(medicineRepository.findByExpiryDateBefore(any(LocalDate.class)))
                .thenReturn(List.of());

        // Act
        List<MedicineResponse> result = reportService.getExpiredMedicines();

        // Assert
        assertThat(result).isEmpty();
    }

    // ── getInventoryAudit ─────────────────────────────────────────────────

    @Test
    @DisplayName("getInventoryAudit: returns low-stock medicines with supplier contact details")
    void getInventoryAudit_returnsAuditList() {
        // Arrange
        Medicine med = buildMedicineWithSupplier();
        when(medicineRepository.findLowStockWithSupplier(50)).thenReturn(List.of(med));

        // Act
        List<InventoryAuditResponse> result = reportService.getInventoryAudit();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMedicineName()).isEqualTo("Dolo 650");
        assertThat(result.get(0).getSupplierName()).isEqualTo("Apollo Distributors");
        assertThat(result.get(0).getSupplierPhone()).isEqualTo("022-123456");
        assertThat(result.get(0).getStockQuantity()).isEqualTo(20);
    }
}
