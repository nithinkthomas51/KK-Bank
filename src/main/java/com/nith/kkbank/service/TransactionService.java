package com.nith.kkbank.service;

import com.nith.kkbank.dto.CreditDebitTransactionDetails;

public interface TransactionService {

    void creditTransaction(CreditDebitTransactionDetails details);
    void debitTransaction(CreditDebitTransactionDetails details);
}
