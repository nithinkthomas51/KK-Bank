package com.nith.kkbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionInfo {
    private String transactionId;
    private String transactionType;
    private BigDecimal transactionAmount;
}
