package com.nith.kkbank.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferEvent extends ApplicationEvent {

    private String sourceUserEmail;
    private String destinationUserEmail;
    private String sourceUserName;
    private String destinationUserName;
    private String sourceUserAccountNumber;
    private String destinationUserAccountNumber;
    private BigDecimal sourceAccountBalance;
    private BigDecimal destinationAccountBalance;
    private BigDecimal amount;

    public TransferEvent(Object source,
                         String sourceUserEmail,
                         String destinationUserEmail,
                         String sourceUserName,
                         String destinationUserName,
                         String sourceUserAccountNumber,
                         String destinationUserAccountNumber,
                         BigDecimal sourceAccountBalance,
                         BigDecimal destinationAccountBalance,
                         BigDecimal amount) {
        super(source);
        this.sourceUserEmail = sourceUserEmail;
        this.destinationUserEmail = destinationUserEmail;
        this.sourceUserName = sourceUserName;
        this.destinationUserName = destinationUserName;
        this.sourceUserAccountNumber = sourceUserAccountNumber;
        this.destinationUserAccountNumber = destinationUserAccountNumber;
        this.sourceAccountBalance = sourceAccountBalance;
        this.destinationAccountBalance = destinationAccountBalance;
        this.amount = amount;
    }
}
