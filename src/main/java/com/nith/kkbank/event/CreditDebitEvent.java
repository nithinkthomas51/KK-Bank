package com.nith.kkbank.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
@Setter
public class CreditDebitEvent extends ApplicationEvent {

    private String emailId;
    private String accountNumber;
    private BigDecimal currentBalance;
    private BigDecimal amount;
    private String transactionType;

    public CreditDebitEvent(Object source, String emailId, String accountNumber, BigDecimal currentBalance, BigDecimal amount, String transactionType) {
        super(source);
        this.emailId = emailId;
        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
        this.amount = amount;
        this.transactionType = transactionType;
    }
}
