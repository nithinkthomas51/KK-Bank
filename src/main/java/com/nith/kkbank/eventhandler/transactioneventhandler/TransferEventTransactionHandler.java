package com.nith.kkbank.eventhandler.transactioneventhandler;

import com.nith.kkbank.dto.TransactionDetails;
import com.nith.kkbank.event.TransferEvent;
import com.nith.kkbank.service.TransactionService;
import com.nith.kkbank.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferEventTransactionHandler {

    @Autowired
    TransactionService transactionService;

    @EventListener
    @Async
    void process(TransferEvent event) {

        TransactionDetails sourceDetails = TransactionDetails.builder()
                .accountNumber(event.getSourceUserAccountNumber())
                .debitAmount(event.getAmount())
                .creditAmount(BigDecimal.ZERO)
                .transactionType(TransactionUtils.TRANSFER)
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .transactionDescription(TransactionUtils.createTransferDescription(event.getDestinationUserAccountNumber(), false))
                .build();

        TransactionDetails destinationDetails = TransactionDetails.builder()
                .accountNumber(event.getDestinationUserAccountNumber())
                .creditAmount(event.getAmount())
                .debitAmount(BigDecimal.ZERO)
                .transactionDescription(TransactionUtils.createTransferDescription(event.getSourceUserAccountNumber(), true))
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .transactionType(TransactionUtils.TRANSFER)
                .build();
        saveTransactionDetails(sourceDetails);
        saveTransactionDetails(destinationDetails);
    }

    @Async
    void saveTransactionDetails(TransactionDetails details) {
        transactionService.saveTransaction(details);
    }
}
