package com.nith.kkbank.eventhandler;

import com.nith.kkbank.dto.EmailDetails;
import com.nith.kkbank.event.TransferEvent;
import com.nith.kkbank.service.EmailService;
import com.nith.kkbank.utils.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TransferEventEmailHandler {

    @Autowired
    EmailService emailService;

    @EventListener
    @Async
    void process(TransferEvent event) {
        EmailDetails sourceDetails = EmailUtils.generateTransferEmailDetailsForSource(
                event.getSourceUserEmail(),
                event.getDestinationUserAccountNumber(),
                event.getDestinationUserName(),
                event.getSourceAccountBalance(),
                event.getAmount());

        EmailDetails destinationEmailDetails = EmailUtils.generateTransferEmailDetailsForDestination(
                event.getDestinationUserEmail(),
                event.getSourceUserAccountNumber(),
                event.getSourceUserName(),
                event.getDestinationAccountBalance(),
                event.getAmount());

        sendMailToSource(sourceDetails);
        sendMailToDestination(destinationEmailDetails);
    }

    @Async
    private void sendMailToSource(EmailDetails sourceEmailDetails) {
        emailService.sendEmailAlert(sourceEmailDetails);
    }

    @Async
    private void sendMailToDestination(EmailDetails destinationEmailDetails) {
        emailService.sendEmailAlert(destinationEmailDetails);
    }
}
