package com.nith.kkbank.service.impl;

import com.nith.kkbank.dto.CreditDebitTransactionDetails;
import com.nith.kkbank.entity.Transaction;
import com.nith.kkbank.repository.TransactionRepository;
import com.nith.kkbank.service.TransactionService;
import com.nith.kkbank.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public void creditTransaction(CreditDebitTransactionDetails details) {
        Transaction creditTransaction = Transaction.builder()
                .accountNumber(details.getAccountNumber())
                .creditAmount(details.getAmount())
                .debitAmount(BigDecimal.ZERO)
                .transactionType(TransactionUtils.CREDIT)
                .transactionDescription(TransactionUtils.createCreditDescription())
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .build();
        transactionRepository.save(creditTransaction);
    }

    @Override
    public void debitTransaction(CreditDebitTransactionDetails details) {
        Transaction creditTransaction = Transaction.builder()
                .accountNumber(details.getAccountNumber())
                .creditAmount(BigDecimal.ZERO)
                .debitAmount(details.getAmount())
                .transactionType(TransactionUtils.DEBIT)
                .transactionDescription(TransactionUtils.createDebitDescription())
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .build();
        transactionRepository.save(creditTransaction);
    }
}
