package com.nith.kkbank.eventhandler.transactioneventhandler;

import com.nith.kkbank.dto.TransactionDetails;
import com.nith.kkbank.event.CreditDebitEvent;
import com.nith.kkbank.service.TransactionService;
import com.nith.kkbank.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreditDebitEventTransactionHandler {

    @Autowired
    TransactionService transactionService;

    @EventListener
    @Async
    void process(CreditDebitEvent event) {

        String transactionType = event.getTransactionType();

        if (!(transactionType.equals(TransactionUtils.CREDIT) || transactionType.equals(TransactionUtils.DEBIT))) {
            return;
        }

        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(event.getAccountNumber());
        transactionDetails.setTransactionType(transactionType);
        transactionDetails.setTransactionStatus(TransactionUtils.TRANSACTION_COMPLETE);

        if (transactionType.equals(TransactionUtils.CREDIT)) {
            transactionDetails.setCreditAmount(event.getAmount());
            transactionDetails.setDebitAmount(BigDecimal.ZERO);
            transactionDetails.setTransactionDescription(TransactionUtils.createCreditDescription());
        } else {
            transactionDetails.setCreditAmount(BigDecimal.ZERO);
            transactionDetails.setDebitAmount(event.getAmount());
            transactionDetails.setTransactionDescription(TransactionUtils.createDebitDescription());
        }
        transactionService.saveTransaction(transactionDetails);
    }
}
