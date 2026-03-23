package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.CustomerRequest;
import com.codegnan.jeevanraksha.dto.response.CustomerResponse;
import com.codegnan.jeevanraksha.dto.response.CustomerSummaryResponse;
import com.codegnan.jeevanraksha.dto.response.OrderResponse;
import com.codegnan.jeevanraksha.dto.response.TopSpenderResponse;
import com.codegnan.jeevanraksha.entity.Customer;
import com.codegnan.jeevanraksha.entity.Order;
import com.codegnan.jeevanraksha.enums.PaymentMode;
import com.codegnan.jeevanraksha.exception.ResourceConstraintException;
import com.codegnan.jeevanraksha.exception.ResourceNotFoundException;
import com.codegnan.jeevanraksha.repository.CustomerRepository;
import com.codegnan.jeevanraksha.repository.OrderRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomerService}.
 *
 * <p>Uses {@code @ExtendWith(MockitoExtension.class)} so the Spring context is
 * never loaded — tests execute fast and in isolation. All dependencies are
 * replaced with Mockito mocks; only the service logic under test is real.</p>
 *
 * <p><strong>Naming convention:</strong>
 * {@code methodName_whenCondition_thenExpectedOutcome()}</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    // ── Mocks (all dependencies of CustomerService) ───────────────────────
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    // ── Subject under test ────────────────────────────────────────────────
    @InjectMocks
    private CustomerService customerService;

    // ── Test data helpers ─────────────────────────────────────────────────

    private Customer buildCustomer() {
        Customer c = new Customer();
        c.setCustomerId(1);
        c.setName("Rahul Sharma");
        c.setPhone("9876543210");
        c.setCity("Mumbai");
        return c;
    }

    private CustomerRequest buildRequest() {
        CustomerRequest req = new CustomerRequest();
        req.setName("Rahul Sharma");
        req.setPhone("9876543210");
        req.setCity("Mumbai");
        return req;
    }

    // ── getAllCustomers ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCustomers: no city filter → returns all customers paginated")
    void getAllCustomers_whenNoCityFilter_returnsAllCustomers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(List.of(buildCustomer()));
        when(customerRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<CustomerResponse> result = customerService.getAllCustomers(null, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Rahul Sharma");
        verify(customerRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getAllCustomers: city filter provided → calls city-filtered repository method")
    void getAllCustomers_whenCityFilter_callsFilteredQuery() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(List.of(buildCustomer()));
        when(customerRepository.findByCityIgnoreCase("Mumbai", pageable)).thenReturn(page);

        // Act
        Page<CustomerResponse> result = customerService.getAllCustomers("Mumbai", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        verify(customerRepository).findByCityIgnoreCase("Mumbai", pageable);
        verify(customerRepository, never()).findAll(pageable);
    }

    // ── getCustomerById ───────────────────────────────────────────────────

    @Test
    @DisplayName("getCustomerById: existing ID → returns summary with order stats")
    void getCustomerById_whenExists_returnsCustomerSummary() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(buildCustomer()));
        when(customerRepository.findOrderStatsByCustomerId(1))
                .thenReturn(new Object[]{3L, new BigDecimal("750.00")});

        // Act
        CustomerSummaryResponse result = customerService.getCustomerById(1);

        // Assert
        assertThat(result.getCustomerId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Rahul Sharma");
        assertThat(result.getTotalOrders()).isEqualTo(3L);
        assertThat(result.getTotalAmountSpent()).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("getCustomerById: non-existent ID → throws ResourceNotFoundException")
    void getCustomerById_whenNotExists_throwsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomerById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining("99");
    }

    // ── createCustomer ────────────────────────────────────────────────────

    @Test
    @DisplayName("createCustomer: valid request → saves and returns CustomerResponse")
    void createCustomer_validRequest_returnsCreatedCustomer() {
        // Arrange
        Customer saved = buildCustomer();
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        // Act
        CustomerResponse result = customerService.createCustomer(buildRequest());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Rahul Sharma");
        assertThat(result.getCity()).isEqualTo("Mumbai");
        verify(customerRepository).save(any(Customer.class));
    }

    // ── updateCustomer ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateCustomer: existing ID → updates fields and returns updated response")
    void updateCustomer_whenExists_returnsUpdatedCustomer() {
        // Arrange
        Customer existing = buildCustomer();
        when(customerRepository.findById(1)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);

        CustomerRequest updateReq = new CustomerRequest();
        updateReq.setName("Rahul S");
        updateReq.setPhone("9000000000");
        updateReq.setCity("Pune");

        // Act
        CustomerResponse result = customerService.updateCustomer(1, updateReq);

        // Assert
        assertThat(result.getName()).isEqualTo("Rahul S");
        assertThat(result.getCity()).isEqualTo("Pune");
        verify(customerRepository).save(existing);
    }

    @Test
    @DisplayName("updateCustomer: non-existent ID → throws ResourceNotFoundException")
    void updateCustomer_whenNotExists_throwsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.updateCustomer(99, buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteCustomer ────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCustomer: customer has no orders → deletes successfully")
    void deleteCustomer_whenNoOrders_deletesSuccessfully() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(buildCustomer()));
        when(customerRepository.countOrdersByCustomerId(1)).thenReturn(0L);

        // Act
        customerService.deleteCustomer(1);

        // Assert
        verify(customerRepository).deleteById(1);
    }

    @Test
    @DisplayName("deleteCustomer: customer has linked orders → throws ResourceConstraintException")
    void deleteCustomer_whenHasOrders_throwsResourceConstraintException() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(buildCustomer()));
        when(customerRepository.countOrdersByCustomerId(1)).thenReturn(3L);

        // Act & Assert
        assertThatThrownBy(() -> customerService.deleteCustomer(1))
                .isInstanceOf(ResourceConstraintException.class)
                .hasMessageContaining("3 order(s)");

        verify(customerRepository, never()).deleteById(any());
    }

    // ── getCustomerOrders ─────────────────────────────────────────────────

    @Test
    @DisplayName("getCustomerOrders: existing customer → delegates to orderService for conversion")
    void getCustomerOrders_whenCustomerExists_returnsOrderList() {
        // Arrange
        Customer customer = buildCustomer();
        Order order = new Order();
        order.setOrderId(10);
        order.setCustomer(customer);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setPaymentMode(PaymentMode.UPI);

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomerCustomerId(1)).thenReturn(List.of(order));
        when(orderService.toOrderResponse(order)).thenReturn(
                OrderResponse.builder().orderId(10).build());

        // Act
        List<OrderResponse> result = customerService.getCustomerOrders(1);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(10);
    }

    // ── getTopSpenders ────────────────────────────────────────────────────

    @Test
    @DisplayName("getTopSpenders: returns customers ranked by total spend")
    void getTopSpenders_returnsRankedList() {
        // Arrange
        Customer c = buildCustomer();
        Object[] row = new Object[]{c, new BigDecimal("1500.00")};
        List<Object[]> spenderRows = new ArrayList<>();
        spenderRows.add(row);
        when(customerRepository.findTopSpenders()).thenReturn(spenderRows);

        // Act
        List<TopSpenderResponse> result = customerService.getTopSpenders();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Rahul Sharma");
        assertThat(result.get(0).getTotalAmountSpent()).isEqualByComparingTo("1500.00");
    }
}
