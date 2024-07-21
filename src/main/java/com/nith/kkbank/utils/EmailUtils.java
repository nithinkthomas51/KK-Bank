package com.nith.kkbank.utils;

import com.nith.kkbank.dto.EmailDetails;

import java.math.BigDecimal;

public class EmailUtils {

    private static final String CUSTOMER_SUPPORT = "knkbankcustomersupport@gmail.com";

    public static EmailDetails generateTransferEmailDetailsForSource(
             String sourceUserEmail,
             String destinationAccountNumber,
             String destinationUserName,
             BigDecimal sourceAccountBalance,
             BigDecimal transferAmount) {

                    return EmailDetails.builder()
                            .subject("DEBIT ALERT")
                            .recipient(sourceUserEmail)
                            .messageBody("Your transfer of "
                                    + transferAmount
                                    + " rupees to the account "
                                    + destinationAccountNumber
                                    + " ("
                                    + destinationUserName
                                    + ") is successful."
                                    + "\nYour current balance : "
                                    + sourceAccountBalance)
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
                            .messageBody("Amount Credited : "
                                    + transferAmount
                                    + "\nTransferred by : "
                                    + sourceAccountNumber
                                    + "("
                                    + sourceUserName
                                    + ")"
                                    + "\nYour current balance : "
                                    + destinationAccountBalance)
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
            emailDetails.setMessageBody("Your account "
                    + accountNumber
                    + " is credited with "
                    + amount
                    + " rupees.\nYour current balance : "
                    + currentBalance);
        } else if (transactionType.equals(TransactionUtils.DEBIT)){
            emailDetails.setMessageBody(amount
                    + " rupees spent on your account "
                    + accountNumber
                    + ".\nYour current Balance : "
                    + currentBalance
                    + "\nIf not done by you, please contact KnK Bank customer support\n"
                    + CUSTOMER_SUPPORT);
        } else {
            emailDetails.setMessageBody("Noticed an unknown transaction of "
                    + amount
                    + " rupees on your account "
                    + accountNumber
                    + "\nIf not done by you, please contact KnK Bank customer support\n"
                    + CUSTOMER_SUPPORT);
        }
        return emailDetails;
    }
}
