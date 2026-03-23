package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.OrderItemRequest;
import com.codegnan.jeevanraksha.dto.request.OrderRequest;
import com.codegnan.jeevanraksha.dto.response.InvoiceResponse;
import com.codegnan.jeevanraksha.dto.response.OrderResponse;
import com.codegnan.jeevanraksha.entity.*;
import com.codegnan.jeevanraksha.enums.PaymentMode;
import com.codegnan.jeevanraksha.exception.InsufficientStockException;
import com.codegnan.jeevanraksha.exception.ResourceNotFoundException;
import com.codegnan.jeevanraksha.repository.CustomerRepository;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import com.codegnan.jeevanraksha.repository.OrderItemRepository;
import com.codegnan.jeevanraksha.repository.OrderRepository;
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
 * Unit tests for {@link OrderService}.
 *
 * <p>This is the most important service to test because it coordinates
 * multi-step atomic operations: stock validation, deduction, order
 * creation, and cancellation with stock restoration.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private OrderService orderService;

    // ── Test data helpers ─────────────────────────────────────────────────

    private Customer buildCustomer() {
        Customer c = new Customer();
        c.setCustomerId(1);
        c.setName("Rahul Sharma");
        c.setPhone("9876543210");
        c.setCity("Mumbai");
        return c;
    }

    private Supplier buildSupplier() {
        Supplier s = new Supplier();
        s.setSupplierId(1);
        s.setSupplierName("Apollo Distributors");
        s.setContactPerson("Rajesh Gupta");
        s.setPhone("022-123456");
        return s;
    }

    private Medicine buildMedicine(int stock) {
        Medicine m = new Medicine();
        m.setMedicineId(1);
        m.setName("Dolo 650");
        m.setCategory("Tablet");
        m.setPrice(new BigDecimal("30.00"));
        m.setStockQuantity(stock);
        m.setExpiryDate(LocalDate.now().plusMonths(12));
        m.setSupplier(buildSupplier());
        return m;
    }

    private Order buildSavedOrder(Customer customer) {
        Order o = new Order();
        o.setOrderId(10);
        o.setCustomer(customer);
        o.setOrderDate(LocalDate.now());
        o.setPaymentMode(PaymentMode.UPI);
        o.setTotalAmount(BigDecimal.ZERO);
        o.setOrderItems(new ArrayList<>());
        return o;
    }

    private OrderRequest buildOrderRequest(int qty) {
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setMedicineId(1);
        itemReq.setQuantity(qty);

        OrderRequest req = new OrderRequest();
        req.setCustomerId(1);
        req.setPaymentMode(PaymentMode.UPI);
        req.setItems(List.of(itemReq));
        return req;
    }

    // ── placeOrder ────────────────────────────────────────────────────────

    @Test
    @DisplayName("placeOrder: sufficient stock → deducts stock and persists order")
    void placeOrder_whenSufficientStock_deductsStockAndCreatesOrder() {
        // Arrange
        Customer customer = buildCustomer();
        Medicine medicine = buildMedicine(100);   // 100 in stock
        Order savedOrder = buildSavedOrder(customer);

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(medicineRepository.findById(1)).thenReturn(Optional.of(medicine));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);

        OrderItem savedItem = new OrderItem();
        savedItem.setItemId(1);
        savedItem.setOrder(savedOrder);
        savedItem.setMedicine(medicine);
        savedItem.setQuantity(5);
        savedItem.setSubtotal(new BigDecimal("150.00"));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedItem);
        when(orderItemRepository.findByOrderOrderId(10)).thenReturn(List.of(savedItem));

        // Act
        OrderResponse result = orderService.placeOrder(buildOrderRequest(5));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(10);
        // Stock should have been saved (deducted)
        verify(medicineRepository, atLeastOnce()).save(any(Medicine.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    @DisplayName("placeOrder: requested quantity exceeds stock → throws InsufficientStockException")
    void placeOrder_whenInsufficientStock_throwsInsufficientStockException() {
        // Arrange — only 3 units available, order requests 10
        Customer customer = buildCustomer();
        Medicine medicine = buildMedicine(3);

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(medicineRepository.findById(1)).thenReturn(Optional.of(medicine));

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(buildOrderRequest(10)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Dolo 650")
                .hasMessageContaining("3")    // available
                .hasMessageContaining("10");  // requested

        // Ensure no order was saved
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("placeOrder: customer does not exist → throws ResourceNotFoundException")
    void placeOrder_whenCustomerNotFound_throwsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findById(99)).thenReturn(Optional.empty());

        OrderRequest req = buildOrderRequest(1);
        req.setCustomerId(99);

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer");
    }

    @Test
    @DisplayName("placeOrder: medicine does not exist → throws ResourceNotFoundException")
    void placeOrder_whenMedicineNotFound_throwsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(buildCustomer()));
        when(medicineRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(buildOrderRequest(1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medicine");
    }

    // ── getOrderById ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrderById: existing ID → returns full OrderResponse with items")
    void getOrderById_whenExists_returnsOrderResponse() {
        // Arrange
        Customer customer = buildCustomer();
        Order order = buildSavedOrder(customer);
        Medicine med = buildMedicine(50);

        OrderItem item = new OrderItem();
        item.setItemId(1);
        item.setOrder(order);
        item.setMedicine(med);
        item.setQuantity(2);
        item.setSubtotal(new BigDecimal("60.00"));

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderOrderId(10)).thenReturn(List.of(item));

        // Act
        OrderResponse result = orderService.getOrderById(10);

        // Assert
        assertThat(result.getOrderId()).isEqualTo(10);
        assertThat(result.getCustomerName()).isEqualTo("Rahul Sharma");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getMedicineName()).isEqualTo("Dolo 650");
    }

    @Test
    @DisplayName("getOrderById: non-existent ID → throws ResourceNotFoundException")
    void getOrderById_whenNotExists_throwsResourceNotFoundException() {
        // Arrange
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order");
    }

    // ── cancelOrder ───────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelOrder: existing order → restores stock for each item then deletes order")
    void cancelOrder_restoresStockAndDeletesOrder() {
        // Arrange
        Customer customer = buildCustomer();
        Order order = buildSavedOrder(customer);
        Medicine med = buildMedicine(50); // current stock = 50

        OrderItem item = new OrderItem();
        item.setItemId(1);
        item.setOrder(order);
        item.setMedicine(med);
        item.setQuantity(5); // should be restored

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderOrderId(10)).thenReturn(List.of(item));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(med);

        // Act
        orderService.cancelOrder(10);

        // Assert
        // Stock should be restored: 50 + 5 = 55
        assertThat(med.getStockQuantity()).isEqualTo(55);
        verify(medicineRepository).save(med);
        verify(orderRepository).deleteById(10);
    }

    @Test
    @DisplayName("cancelOrder: non-existent order → throws ResourceNotFoundException")
    void cancelOrder_whenNotFound_throwsResourceNotFoundException() {
        // Arrange
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(99))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).deleteById(any());
    }

    // ── getOrdersByPaymentMode ────────────────────────────────────────────

    @Test
    @DisplayName("getOrdersByPaymentMode: UPI → returns only UPI orders")
    void getOrdersByPaymentMode_returnsFilteredOrders() {
        // Arrange
        Customer customer = buildCustomer();
        Order order = buildSavedOrder(customer);
        order.setPaymentMode(PaymentMode.UPI);

        when(orderRepository.findByPaymentMode(PaymentMode.UPI)).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderOrderId(10)).thenReturn(List.of());

        // Act
        List<OrderResponse> result = orderService.getOrdersByPaymentMode(PaymentMode.UPI);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaymentMode()).isEqualTo(PaymentMode.UPI);
    }

    // ── getInvoice ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getInvoice: existing order → returns invoice with pharmacy details")
    void getInvoice_whenExists_returnsInvoiceResponse() {
        // Arrange
        Customer customer = buildCustomer();
        Order order = buildSavedOrder(customer);
        order.setTotalAmount(new BigDecimal("150.00"));

        Medicine med = buildMedicine(50);
        OrderItem item = new OrderItem();
        item.setItemId(1);
        item.setOrder(order);
        item.setMedicine(med);
        item.setQuantity(5);
        item.setSubtotal(new BigDecimal("150.00"));

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderOrderId(10)).thenReturn(List.of(item));

        // Act
        InvoiceResponse invoice = orderService.getInvoice(10);

        // Assert
        assertThat(invoice.getInvoiceNumber()).isEqualTo(10);
        assertThat(invoice.getCustomerName()).isEqualTo("Rahul Sharma");
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo("150.00");
        assertThat(invoice.getPharmacyName()).isEqualTo("Jeevan Raksha Pharmacy");
        assertThat(invoice.getItems()).hasSize(1);
    }
}
