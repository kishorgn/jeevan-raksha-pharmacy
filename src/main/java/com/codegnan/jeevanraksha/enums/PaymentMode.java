package com.codegnan.jeevanraksha.enums;

/**
 * Represents the supported payment modes for pharmacy orders.
 *
 * <p>Maps directly to the MySQL ENUM column {@code payment_mode}
 * in the {@code orders} table.</p>
 */
public enum PaymentMode {

    /** Unified Payments Interface – digital bank transfer. */
    UPI,

    /** Physical cash transaction. */
    Cash,

    /** Debit or credit card payment. */
    Card
}
