package com.nith.kkbank.eventhandler.transactioneventhandler;

import com.nith.kkbank.dto.CreditDebitTransactionDetails;
import com.nith.kkbank.event.CreditDebitEvent;
import com.nith.kkbank.service.TransactionService;
import com.nith.kkbank.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CreditDebitEventTransactionHandler {

    @Autowired
    TransactionService transactionService;

    @EventListener
    @Async
    void process(CreditDebitEvent event) {
        String transactionType = event.getTransactionType();
        CreditDebitTransactionDetails transactionDetails = CreditDebitTransactionDetails.builder()
                .accountNumber(event.getAccountNumber())
                .amount(event.getAmount())
                .build();
        if (transactionType.equals(TransactionUtils.CREDIT)) {
            transactionService.creditTransaction(transactionDetails);
        } else {
            transactionService.debitTransaction(transactionDetails);
        }
    }
}
