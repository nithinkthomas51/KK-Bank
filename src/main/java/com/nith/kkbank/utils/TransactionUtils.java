package com.nith.kkbank.utils;

import java.math.BigDecimal;

public class TransactionUtils {

    // Transaction Types
    public static final String CREDIT = "CREDIT";
    public static final String DEBIT = "DEBIT";
    public static final String TRANSFER = "TRANSFER";

    // Transaction Status
    public static final String TRANSACTION_COMPLETE = "COMPLETE";
    public static final String TRANSACTION_PENDING = "PENDING";
    public static final String TRANSACTION_FAILED = "FAILED";

    public static String createCreditDescription() {
        return "Credited via online banking";
    }

    public static String createDebitDescription() {
        return "Debited via online banking";
    }

    public static String createTransferDescription(String accountNumber, boolean credited) {
        if (credited) {
            return "Transferred via online banking by " + accountNumber;
        }
        return "Transferred via online banking to " + accountNumber;
    }
}
