package com.nith.kkbank.service;

import com.nith.kkbank.dto.TransactionDetails;

public interface TransactionService {

    void saveTransaction(TransactionDetails details);
}
