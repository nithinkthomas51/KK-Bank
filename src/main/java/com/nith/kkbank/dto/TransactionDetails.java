package com.nith.kkbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetails {
    private String accountNumber;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    private String transactionType;
    private String transactionDescription;
    private String transactionStatus;
}
