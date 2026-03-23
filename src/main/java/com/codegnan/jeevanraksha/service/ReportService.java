package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.response.*;
import com.codegnan.jeevanraksha.entity.Customer;
import com.codegnan.jeevanraksha.entity.Medicine;
import com.codegnan.jeevanraksha.enums.PaymentMode;
import com.codegnan.jeevanraksha.exception.InvalidRequestException;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import com.codegnan.jeevanraksha.repository.OrderItemRepository;
import com.codegnan.jeevanraksha.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for all reporting and analytics operations.
 *
 * <p>All report methods are read-only; they aggregate data from the
 * orders, order_items, medicines, and customers tables using
 * purpose-built JPQL queries.</p>
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    /** Default low-stock threshold used by the inventory audit report. */
    private static final int AUDIT_STOCK_THRESHOLD = 50;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineService medicineService;

    public ReportService(OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         MedicineRepository medicineRepository,
                         MedicineService medicineService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.medicineRepository = medicineRepository;
        this.medicineService = medicineService;
    }

    // ---------------------------------------------------------------
    // GET /api/reports/revenue?from=&to=
    // ---------------------------------------------------------------

    /**
     * Calculates total revenue within an inclusive date range.
     *
     * @throws InvalidRequestException if {@code from} is after {@code to}
     */
    public RevenueResponse getRevenue(LocalDate from, LocalDate to) {
        logger.debug("Calculating revenue from {} to {}", from, to);
        if (from.isAfter(to)) {
            throw new InvalidRequestException(
                    "'from' date must not be after 'to' date");
        }
        BigDecimal total = orderRepository.calculateRevenueBetween(from, to);
        long count = orderRepository.countOrdersBetween(from, to);
        return RevenueResponse.builder()
                .from(from)
                .to(to)
                .totalRevenue(total != null ? total : BigDecimal.ZERO)
                .orderCount(count)
                .build();
    }

    // ---------------------------------------------------------------
    // GET /api/reports/revenue-by-payment-mode
    // ---------------------------------------------------------------

    /**
     * Returns revenue and order count broken down by payment mode.
     */
    public List<RevenueByModeResponse> getRevenueByPaymentMode() {
        logger.debug("Calculating revenue by payment mode");
        List<Object[]> results = orderRepository.findRevenueByPaymentMode();
        return results.stream().map(row -> {
            PaymentMode mode = (PaymentMode) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            long count = ((Number) row[2]).longValue();
            return RevenueByModeResponse.builder()
                    .paymentMode(mode)
                    .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .orderCount(count)
                    .build();
        }).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // GET /api/reports/bestsellers
    // ---------------------------------------------------------------

    /**
     * Returns all medicines ranked by units sold, descending.
     */
    public List<BestsellerResponse> getBestsellers() {
        logger.debug("Fetching bestseller medicines");
        List<Object[]> results = orderItemRepository.findBestsellers();
        return results.stream().map(row -> BestsellerResponse.builder()
                .medicineId(((Number) row[0]).intValue())
                .medicineName((String) row[1])
                .category((String) row[2])
                .price((BigDecimal) row[3])
                .totalQuantitySold(((Number) row[4]).longValue())
                .totalRevenue((BigDecimal) row[5])
                .build()
        ).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // GET /api/reports/customer-with-most-orders
    // ---------------------------------------------------------------

    /**
     * Returns the customer who has placed the most orders.
     */
    public CustomerOrderCountResponse getCustomerWithMostOrders() {
        logger.debug("Finding customer with most orders");
        List<Object[]> results = orderRepository.findCustomerRankedByOrderCount(
                PageRequest.of(0, 1));
        if (results.isEmpty()) {
            throw new InvalidRequestException("No orders exist yet. Cannot determine top customer.");
        }
        Object[] row = results.get(0);
        Customer c = (Customer) row[0];
        long count = ((Number) row[1]).longValue();
        return CustomerOrderCountResponse.builder()
                .customerId(c.getCustomerId())
                .name(c.getName())
                .city(c.getCity())
                .phone(c.getPhone())
                .orderCount(count)
                .build();
    }

    // ---------------------------------------------------------------
    // GET /api/reports/expired-medicines
    // ---------------------------------------------------------------

    /**
     * Returns medicines whose expiry date is in the past.
     */
    public List<MedicineResponse> getExpiredMedicines() {
        logger.debug("Fetching expired medicines");
        return medicineRepository.findByExpiryDateBefore(LocalDate.now())
                .stream().map(medicineService::toResponse).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // GET /api/reports/inventory-audit
    // ---------------------------------------------------------------

    /**
     * Returns low-stock medicines (stock &le; 50) with their supplier
     * contact details — ready to use as a reorder sheet.
     */
    public List<InventoryAuditResponse> getInventoryAudit() {
        logger.debug("Generating inventory audit report");
        List<Medicine> lowStock = medicineRepository.findLowStockWithSupplier(AUDIT_STOCK_THRESHOLD);
        return lowStock.stream().map(m -> InventoryAuditResponse.builder()
                .medicineId(m.getMedicineId())
                .medicineName(m.getName())
                .category(m.getCategory())
                .stockQuantity(m.getStockQuantity())
                .expiryDate(m.getExpiryDate())
                .supplierId(m.getSupplier().getSupplierId())
                .supplierName(m.getSupplier().getSupplierName())
                .supplierContact(m.getSupplier().getContactPerson())
                .supplierPhone(m.getSupplier().getPhone())
                .build()
        ).collect(Collectors.toList());
    }
}
