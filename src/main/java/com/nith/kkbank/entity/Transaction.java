package com.nith.kkbank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String transactionID;
    private String transactionType;
    private String transactionDescription;
    private String accountNumber;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    private String transactionStatus;
    @CreationTimestamp
    private LocalDateTime transactionDateTime;
    @UpdateTimestamp
    private LocalDateTime transactionUpdateTime;
}
