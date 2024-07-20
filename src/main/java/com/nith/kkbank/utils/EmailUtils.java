package com.nith.kkbank.utils;

import com.nith.kkbank.dto.EmailDetails;

import java.math.BigDecimal;

public class EmailUtils {

    public static EmailDetails generateEmailDetailsForSource(String sourceUserEmail,
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

    public static EmailDetails generateEmailDetailsForDestination(String destinationUserEmail,
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
}
