package com.nith.kkbank.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class CreateAccountEvent extends ApplicationEvent {

    private String userName;
    private String accountNumber;
    private String userEmail;

    public CreateAccountEvent(Object source, String userName, String accountNumber, String userEmail) {
        super(source);
        this.userName = userName;
        this.userEmail = userEmail;
        this.accountNumber = accountNumber;
    }
}
