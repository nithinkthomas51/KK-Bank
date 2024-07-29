package com.nith.kkbank.service.impl;

import com.nith.kkbank.dto.TransactionDetails;
import com.nith.kkbank.entity.Transaction;
import com.nith.kkbank.repository.TransactionRepository;
import com.nith.kkbank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public void saveTransaction(TransactionDetails details) {
        Transaction creditTransaction = Transaction.builder()
                .accountNumber(details.getAccountNumber())
                .creditAmount(details.getCreditAmount())
                .debitAmount(details.getDebitAmount())
                .transactionType(details.getTransactionType())
                .transactionDescription(details.getTransactionDescription())
                .transactionStatus(details.getTransactionStatus())
                .build();
        transactionRepository.save(creditTransaction);
    }
}
