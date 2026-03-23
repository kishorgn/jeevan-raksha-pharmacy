package com.codegnan.jeevanraksha.service;

import com.codegnan.jeevanraksha.dto.request.RestockRequest;
import com.codegnan.jeevanraksha.dto.response.MedicineResponse;
import com.codegnan.jeevanraksha.entity.Medicine;
import com.codegnan.jeevanraksha.exception.ResourceNotFoundException;
import com.codegnan.jeevanraksha.repository.MedicineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for inventory management operations.
 *
 * <p>Provides stock overview, low-stock alerting, and restock functionality.
 * Delegates medicine entity conversion to avoid code duplication.</p>
 */
@Service
@Transactional(readOnly = true)
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    /** Default low-stock threshold if none is supplied by the caller. */
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 50;

    private final MedicineRepository medicineRepository;
    private final MedicineService medicineService;

    public InventoryService(MedicineRepository medicineRepository,
                            MedicineService medicineService) {
        this.medicineRepository = medicineRepository;
        this.medicineService = medicineService;
    }

    // ---------------------------------------------------------------
    // GET /api/inventory
    // ---------------------------------------------------------------

    /**
     * Returns the complete stock overview for all medicines.
     */
    public List<MedicineResponse> getAllInventory() {
        logger.debug("Fetching complete inventory");
        return medicineRepository.findAll()
                .stream()
                .map(medicineService::toResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // GET /api/inventory/low-stock?threshold=50
    // ---------------------------------------------------------------

    /**
     * Returns medicines whose stock quantity is at or below the given threshold.
     *
     * <p>Useful for triggering reorder workflows and procurement alerts.</p>
     *
     * @param threshold stock level at or below which a medicine is flagged
     *                  (defaults to {@value #DEFAULT_LOW_STOCK_THRESHOLD} if null)
     */
    public List<MedicineResponse> getLowStockMedicines(Integer threshold) {
        int effectiveThreshold = (threshold != null) ? threshold : DEFAULT_LOW_STOCK_THRESHOLD;
        logger.debug("Fetching low-stock medicines with threshold: {}", effectiveThreshold);
        return medicineRepository.findByStockQuantityLessThanEqual(effectiveThreshold)
                .stream()
                .map(medicineService::toResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // PATCH /api/inventory/{medicineId}/restock
    // ---------------------------------------------------------------

    /**
     * Adds stock to a medicine's inventory (delta restock).
     *
     * <p>The {@code quantity} in the request is a delta value added to the
     * current stock — it does not replace it. This supports partial shipments
     * and multiple restock events for the same medicine.</p>
     *
     * @param medicineId ID of the medicine to restock
     * @param request    contains the delta quantity to add
     * @return updated medicine with the new stock quantity
     */
    @Transactional
    public MedicineResponse restockMedicine(Integer medicineId, RestockRequest request) {
        logger.info("Restocking medicine id {} | adding {} units", medicineId, request.getQuantity());
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));

        int previousStock = medicine.getStockQuantity();
        medicine.setStockQuantity(previousStock + request.getQuantity());
        Medicine updated = medicineRepository.save(medicine);

        logger.info("Medicine id {} restocked: {} -> {} units",
                medicineId, previousStock, updated.getStockQuantity());
        return medicineService.toResponse(updated);
    }
}
