package com.nith.kkbank.eventhandler.emaileventhandler;

import com.nith.kkbank.dto.EmailDetails;
import com.nith.kkbank.event.CreateAccountEvent;
import com.nith.kkbank.service.EmailService;
import com.nith.kkbank.utils.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CreateAccountEmailHandler {

    @Autowired
    private EmailService emailService;

    @EventListener
    @Async
    void process(CreateAccountEvent event) {
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(event.getUserEmail())
                .messageBody("Dear Customer,\n"
                        + "Congratulations! Your Account has been successfully created.\n\n\tAccount Details : \n"
                        + "Account Name : "
                        + event.getUserName()
                        + "\nAccount Number : "
                        + event.getAccountNumber()
                        + "\nIf you have any queries, please contact "
                        + EmailUtils.CUSTOMER_SUPPORT
                        + EmailUtils.WELCOME_STRING
                        + EmailUtils.SALUTATION)
                .subject("Account Creation")
                .build();
        emailService.sendEmailAlert(emailDetails);
    }
}
