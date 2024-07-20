package com.nith.kkbank.event;

import org.springframework.context.ApplicationEvent;

public class CreditEvent extends ApplicationEvent {

    public CreditEvent(Object source) {
        super(source);
    }
}
