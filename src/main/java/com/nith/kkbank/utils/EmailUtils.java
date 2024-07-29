package com.nith.kkbank.utils;

import com.nith.kkbank.dto.EmailDetails;
import org.hibernate.type.internal.CompositeUserTypeJavaTypeWrapper;

import java.math.BigDecimal;

public class EmailUtils {

    public static final String CUSTOMER_SUPPORT = "knkbankcustomersupport@gmail.com";
    public static final String GREETINGS = "Dear Customer,\n";
    public static final String WELCOME_STRING = "\n\nWelcome to KnK Banking family!!!";
    public static final String THANK_YOU = "\n\nThank you for banking with us.";
    public static final String SALUTATION = "\nWARM Regards\nKnK Bank";

    public static EmailDetails generateTransferEmailDetailsForSource(
             String sourceUserEmail,
             String destinationAccountNumber,
             String destinationUserName,
             BigDecimal sourceAccountBalance,
             BigDecimal transferAmount) {

                    return EmailDetails.builder()
                            .subject("DEBIT ALERT")
                            .recipient(sourceUserEmail)
                            .messageBody(GREETINGS
                                    + "Your transfer of "
                                    + transferAmount
                                    + " rupees to the account "
                                    + destinationAccountNumber
                                    + " ("
                                    + destinationUserName.strip()
                                    + ") is successful."
                                    + "\nYour current balance : "
                                    + sourceAccountBalance
                                    + "\nIf not done by you, please contact "
                                    + CUSTOMER_SUPPORT
                                    + THANK_YOU
                                    + SALUTATION)
                            .build();
    }

    public static EmailDetails generateTransferEmailDetailsForDestination(
              String destinationUserEmail,
              String sourceAccountNumber,
              String sourceUserName,
              BigDecimal destinationAccountBalance,
              BigDecimal transferAmount) {

                    return EmailDetails.builder()
                            .recipient(destinationUserEmail)
                            .subject("CREDIT ALERT")
                            .messageBody(GREETINGS
                                    + "Amount Credited : "
                                    + transferAmount
                                    + "\nTransferred by : "
                                    + sourceAccountNumber
                                    + "("
                                    + sourceUserName.strip()
                                    + ")"
                                    + "\nYour current balance : "
                                    + destinationAccountBalance
                                    + THANK_YOU
                                    + SALUTATION)
                            .build();
    }

    public static EmailDetails generateCreditDebitEmailDetails(String emailId,
                                                        String accountNumber,
                                                        BigDecimal amount,
                                                        BigDecimal currentBalance,
                                                        String transactionType)
    {
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(emailId);
        emailDetails.setSubject(transactionType + " ALERT");
        if (transactionType.equals(TransactionUtils.CREDIT)) {
            emailDetails.setMessageBody(GREETINGS
                    + "Your account "
                    + accountNumber
                    + " is credited with "
                    + amount
                    + " rupees.\nYour current balance : "
                    + currentBalance
                    + THANK_YOU
                    + SALUTATION);
        } else if (transactionType.equals(TransactionUtils.DEBIT)){
            emailDetails.setMessageBody(GREETINGS
                    + "RS. " + amount
                    + " spent on your account "
                    + accountNumber
                    + ".\nYour current Balance : "
                    + currentBalance
                    + "\nIf not done by you, please contact "
                    + CUSTOMER_SUPPORT
                    + THANK_YOU
                    + SALUTATION);
        } else {
            emailDetails.setMessageBody(GREETINGS
                    + "Noticed an unknown transaction of "
                    + amount
                    + " rupees on your account "
                    + accountNumber
                    + "\nPlease contact "
                    + CUSTOMER_SUPPORT
                    + " to review this transaction.\n"
                    + SALUTATION);
        }
        return emailDetails;
    }
}
