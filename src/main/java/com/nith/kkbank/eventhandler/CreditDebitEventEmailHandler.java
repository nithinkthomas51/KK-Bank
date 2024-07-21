package com.nith.kkbank.eventhandler;

import com.nith.kkbank.event.CreditDebitEvent;
import com.nith.kkbank.service.EmailService;
import com.nith.kkbank.utils.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CreditDebitEventEmailHandler {

    @Autowired
    EmailService emailService;

    @EventListener
    @Async
    void process(CreditDebitEvent event) {
        emailService.sendEmailAlert(EmailUtils.generateCreditDebitEmailDetails(event.getEmailId(),
                event.getAccountNumber(),
                event.getAmount(),
                event.getCurrentBalance(),
                event.getTransactionType()));
    }
}
