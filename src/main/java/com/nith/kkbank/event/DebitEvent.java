package com.nith.kkbank.event;

import org.springframework.context.ApplicationEvent;

public class DebitEvent extends ApplicationEvent {

    public DebitEvent(Object source) {
        super(source);
    }
}
