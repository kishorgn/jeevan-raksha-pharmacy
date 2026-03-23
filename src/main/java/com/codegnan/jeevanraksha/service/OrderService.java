package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.OrderItemRequest;
import com.codegnan.jeevanraksha.dto.request.OrderRequest;
import com.codegnan.jeevanraksha.dto.response.InvoiceResponse;
import com.codegnan.jeevanraksha.dto.response.OrderItemResponse;
import com.codegnan.jeevanraksha.dto.response.OrderResponse;
import com.codegnan.jeevanraksha.entity.Customer;
import com.codegnan.jeevanraksha.entity.Medicine;
import com.codegnan.jeevanraksha.entity.Order;
import com.codegnan.jeevanraksha.entity.OrderItem;
import com.codegnan.jeevanraksha.enums.PaymentMode;
import com.codegnan.jeevanraksha.exception.InsufficientStockException;
import com.codegnan.jeevanraksha.exception.ResourceNotFoundException;
import com.codegnan.jeevanraksha.repository.CustomerRepository;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import com.codegnan.jeevanraksha.repository.OrderItemRepository;
import com.codegnan.jeevanraksha.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for all Order business logic.
 *
 * <p>The most complex service in the application — handles the full lifecycle
 * of an order: stock validation, atomic stock deduction, order creation,
 * cancellation with stock restoration, and invoice generation.</p>
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private static final String PHARMACY_NAME = "Jeevan Raksha Pharmacy";
    private static final String PHARMACY_LOCATION = "India";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final MedicineRepository medicineRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CustomerRepository customerRepository,
                        MedicineRepository medicineRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.medicineRepository = medicineRepository;
    }

    // ---------------------------------------------------------------
    // POST /api/orders — Place a new order
    // ---------------------------------------------------------------

    /**
     * Places a new order atomically.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Validate customer exists.</li>
     *   <li>For each item: validate medicine exists and has sufficient stock.</li>
     *   <li>Compute subtotals and grand total from live medicine prices.</li>
     *   <li>Deduct stock quantities.</li>
     *   <li>Persist order and all order items.</li>
     * </ol>
     * Wrapped in a single transaction — if any step fails, all changes roll back.
     * </p>
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        logger.info("Placing order for customer id: {}", request.getCustomerId());

        // Step 1 — Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        // Step 2 — Validate all medicines and stock levels upfront
        List<Medicine> medicines = new ArrayList<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            Medicine medicine = medicineRepository.findById(itemReq.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", itemReq.getMedicineId()));
            if (medicine.getStockQuantity() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        medicine.getName(), medicine.getStockQuantity(), itemReq.getQuantity());
            }
            medicines.add(medicine);
        }

        // Step 3 — Build order
        Order order = Order.builder()
                .customer(customer)
                .orderDate(LocalDate.now())
                .paymentMode(request.getPaymentMode())
                .totalAmount(BigDecimal.ZERO) // will update after items
                .orderItems(new ArrayList<>())
                .build();
        Order savedOrder = orderRepository.save(order);

        // Step 4 — Build order items, deduct stock, accumulate total
        BigDecimal grandTotal = BigDecimal.ZERO;
        List<OrderItem> savedItems = new ArrayList<>();

        for (int i = 0; i < request.getItems().size(); i++) {
            OrderItemRequest itemReq = request.getItems().get(i);
            Medicine medicine = medicines.get(i);

            BigDecimal subtotal = medicine.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            grandTotal = grandTotal.add(subtotal);

            // Deduct stock
            medicine.setStockQuantity(medicine.getStockQuantity() - itemReq.getQuantity());
            medicineRepository.save(medicine);

            OrderItem item = OrderItem.builder()
                    .order(savedOrder)
                    .medicine(medicine)
                    .quantity(itemReq.getQuantity())
                    .subtotal(subtotal)
                    .build();
            savedItems.add(orderItemRepository.save(item));
        }

        // Step 5 — Update total amount
        savedOrder.setTotalAmount(grandTotal);
        savedOrder.setOrderItems(savedItems);
        orderRepository.save(savedOrder);

        logger.info("Order id {} placed successfully | total: {}", savedOrder.getOrderId(), grandTotal);
        return toOrderResponse(savedOrder);
    }

    // ---------------------------------------------------------------
    // GET /api/orders/{orderId}
    // ---------------------------------------------------------------

    public OrderResponse getOrderById(Integer orderId) {
        logger.debug("Fetching order id: {}", orderId);
        Order order = findOrderOrThrow(orderId);
        return toOrderResponse(order);
    }

    // ---------------------------------------------------------------
    // GET /api/orders (admin list with date filter)
    // ---------------------------------------------------------------

    public Page<OrderResponse> getAllOrders(LocalDate from, LocalDate to, Pageable pageable) {
        logger.debug("Fetching all orders | from: {} to: {}", from, to);
        Page<Order> page;
        if (from != null && to != null) {
            page = orderRepository.findByOrderDateBetween(from, to, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        return page.map(this::toOrderResponse);
    }

    // ---------------------------------------------------------------
    // GET /api/orders/by-date-range
    // ---------------------------------------------------------------

    public List<OrderResponse> getOrdersByDateRange(LocalDate from, LocalDate to) {
        logger.debug("Fetching orders between {} and {}", from, to);
        return orderRepository.findByOrderDateBetween(from, to)
                .stream().map(this::toOrderResponse).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // DELETE /api/orders/{orderId} — Cancel order + restore stock
    // ---------------------------------------------------------------

    @Transactional
    public void cancelOrder(Integer orderId) {
        logger.info("Cancelling order id: {}", orderId);
        Order order = findOrderOrThrow(orderId);

        // Restore stock for each item
        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
        for (OrderItem item : items) {
            Medicine medicine = item.getMedicine();
            medicine.setStockQuantity(medicine.getStockQuantity() + item.getQuantity());
            medicineRepository.save(medicine);
            logger.debug("Stock restored for medicine id {}: +{} units",
                    medicine.getMedicineId(), item.getQuantity());
        }

        orderRepository.deleteById(orderId);
        logger.info("Order id {} cancelled and stock restored", orderId);
    }

    // ---------------------------------------------------------------
    // GET /api/orders/by-payment-mode/{mode}
    // ---------------------------------------------------------------

    public List<OrderResponse> getOrdersByPaymentMode(PaymentMode mode) {
        logger.debug("Fetching orders by payment mode: {}", mode);
        return orderRepository.findByPaymentMode(mode)
                .stream().map(this::toOrderResponse).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // GET /api/orders/{orderId}/invoice
    // ---------------------------------------------------------------

    public InvoiceResponse getInvoice(Integer orderId) {
        logger.debug("Generating invoice for order id: {}", orderId);
        Order order = findOrderOrThrow(orderId);
        Customer customer = order.getCustomer();
        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return InvoiceResponse.builder()
                .invoiceNumber(order.getOrderId())
                .invoiceDate(order.getOrderDate())
                .customerId(customer.getCustomerId())
                .customerName(customer.getName())
                .customerPhone(customer.getPhone())
                .customerCity(customer.getCity())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .paymentMode(order.getPaymentMode())
                .pharmacyName(PHARMACY_NAME)
                .pharmacyLocation(PHARMACY_LOCATION)
                .build();
    }

    // ---------------------------------------------------------------
    // Public helpers (used by CustomerService, ReportService, etc.)
    // ---------------------------------------------------------------

    /**
     * Converts an {@link Order} entity to an {@link OrderResponse} DTO.
     * Public so that CustomerService can reuse it.
     */
    public OrderResponse toOrderResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getCustomerId())
                .customerName(order.getCustomer().getName())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .paymentMode(order.getPaymentMode())
                .items(itemResponses)
                .build();
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private Order findOrderOrThrow(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        Medicine med = item.getMedicine();
        // unit price = subtotal / quantity (preserves historical pricing)
        BigDecimal unitPrice = item.getQuantity() > 0
                ? item.getSubtotal().divide(BigDecimal.valueOf(item.getQuantity()))
                : med.getPrice();

        return OrderItemResponse.builder()
                .itemId(item.getItemId())
                .medicineId(med.getMedicineId())
                .medicineName(med.getName())
                .medicineCategory(med.getCategory())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .subtotal(item.getSubtotal())
                .build();
    }
}
