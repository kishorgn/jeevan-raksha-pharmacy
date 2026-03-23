package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.CustomerRequest;
import com.codegnan.jeevanraksha.dto.response.CustomerResponse;
import com.codegnan.jeevanraksha.dto.response.CustomerSummaryResponse;
import com.codegnan.jeevanraksha.dto.response.OrderResponse;
import com.codegnan.jeevanraksha.dto.response.TopSpenderResponse;
import com.codegnan.jeevanraksha.entity.Customer;
import com.codegnan.jeevanraksha.entity.Order;
import com.codegnan.jeevanraksha.exception.ResourceConstraintException;
import com.codegnan.jeevanraksha.exception.ResourceNotFoundException;
import com.codegnan.jeevanraksha.repository.CustomerRepository;
import com.codegnan.jeevanraksha.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class containing all business logic for Customer operations.
 *
 * <p>This class sits between the {@link com.codegnan.jeevanraksha.controller.CustomerController}
 * and the {@link CustomerRepository}. It is responsible for:
 * <ul>
 *   <li>Validating business rules (e.g., blocking delete if orders exist).</li>
 *   <li>Converting between entities and response DTOs (no mapper used).</li>
 *   <li>Logging all significant operations.</li>
 * </ul>
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public CustomerService(CustomerRepository customerRepository,
                           OrderRepository orderRepository,
                           OrderService orderService) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    // ---------------------------------------------------------------
    // GET /api/customers
    // ---------------------------------------------------------------

    /**
     * Returns a paginated list of all customers, optionally filtered by city.
     *
     * @param city     optional city filter (null means no filter)
     * @param pageable pagination and sorting parameters
     * @return page of CustomerResponse DTOs
     */
    public Page<CustomerResponse> getAllCustomers(String city, Pageable pageable) {
        logger.debug("Fetching customers | city filter: {}", city);
        Page<Customer> page = (city != null && !city.isBlank())
                ? customerRepository.findByCityIgnoreCase(city, pageable)
                : customerRepository.findAll(pageable);
        return page.map(this::toResponse);
    }

    // ---------------------------------------------------------------
    // GET /api/customers/{customerId}
    // ---------------------------------------------------------------

    /**
     * Returns a customer's full profile with aggregated order statistics.
     */
    public CustomerSummaryResponse getCustomerById(Integer customerId) {
        logger.debug("Fetching customer profile for id: {}", customerId);
        Customer customer = findCustomerOrThrow(customerId);
        Object[] stats = customerRepository.findOrderStatsByCustomerId(customerId);

        long orderCount = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
        BigDecimal totalSpent = stats[1] != null ? (BigDecimal) stats[1] : BigDecimal.ZERO;

        return CustomerSummaryResponse.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .city(customer.getCity())
                .totalOrders(orderCount)
                .totalAmountSpent(totalSpent)
                .build();
    }

    // ---------------------------------------------------------------
    // POST /api/customers
    // ---------------------------------------------------------------

    /**
     * Registers a new customer account.
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        logger.info("Creating new customer: {}", request.getName());
        Customer customer = Customer.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .city(request.getCity())
                .build();
        Customer saved = customerRepository.save(customer);
        logger.info("Customer created with id: {}", saved.getCustomerId());
        return toResponse(saved);
    }

    // ---------------------------------------------------------------
    // PUT /api/customers/{customerId}
    // ---------------------------------------------------------------

    /**
     * Updates an existing customer's profile.
     */
    @Transactional
    public CustomerResponse updateCustomer(Integer customerId, CustomerRequest request) {
        logger.info("Updating customer id: {}", customerId);
        Customer customer = findCustomerOrThrow(customerId);
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setCity(request.getCity());
        Customer updated = customerRepository.save(customer);
        logger.info("Customer updated: {}", updated.getCustomerId());
        return toResponse(updated);
    }

    // ---------------------------------------------------------------
    // DELETE /api/customers/{customerId}
    // ---------------------------------------------------------------

    /**
     * Removes a customer account.
     *
     * <p>Blocked if the customer has any linked orders to prevent orphaned
     * order records.</p>
     */
    @Transactional
    public void deleteCustomer(Integer customerId) {
        logger.info("Attempting to delete customer id: {}", customerId);
        findCustomerOrThrow(customerId);

        long orderCount = customerRepository.countOrdersByCustomerId(customerId);
        if (orderCount > 0) {
            throw new ResourceConstraintException(
                    String.format("Cannot delete customer id %d: %d order(s) are linked to this account. " +
                                  "Please cancel all orders first.", customerId, orderCount));
        }

        customerRepository.deleteById(customerId);
        logger.info("Customer id {} deleted successfully", customerId);
    }

    // ---------------------------------------------------------------
    // GET /api/customers/{customerId}/orders
    // ---------------------------------------------------------------

    /**
     * Returns the full order history for a customer.
     */
    public List<OrderResponse> getCustomerOrders(Integer customerId) {
        logger.debug("Fetching orders for customer id: {}", customerId);
        findCustomerOrThrow(customerId);
        List<Order> orders = orderRepository.findByCustomerCustomerId(customerId);
        return orders.stream()
                .map(orderService::toOrderResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // GET /api/customers/top-spenders
    // ---------------------------------------------------------------

    /**
     * Returns all customers ranked by their total cumulative spending, descending.
     */
    public List<TopSpenderResponse> getTopSpenders() {
        logger.debug("Fetching top spenders");
        List<Object[]> results = customerRepository.findTopSpenders();
        return results.stream().map(row -> {
            Customer c = (Customer) row[0];
            BigDecimal totalSpent = (BigDecimal) row[1];
            return TopSpenderResponse.builder()
                    .customerId(c.getCustomerId())
                    .name(c.getName())
                    .city(c.getCity())
                    .phone(c.getPhone())
                    .totalAmountSpent(totalSpent)
                    .build();
        }).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Looks up a customer by id, throwing {@link ResourceNotFoundException}
     * if not found.
     */
    private Customer findCustomerOrThrow(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
    }

    /**
     * Converts a {@link Customer} entity to a {@link CustomerResponse} DTO.
     * No mapper used — inline conversion here.
     */
    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .city(customer.getCity())
                .build();
    }
}
