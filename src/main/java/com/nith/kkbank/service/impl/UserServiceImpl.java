package com.nith.kkbank.service.impl;
import com.nith.kkbank.dto.*;
import com.nith.kkbank.entity.Transaction;
import com.nith.kkbank.entity.User;
import com.nith.kkbank.repository.TransactionRepository;
import com.nith.kkbank.repository.UserRepository;
import com.nith.kkbank.service.EmailService;
import com.nith.kkbank.service.UserService;
import com.nith.kkbank.utils.AccountUtils;
import com.nith.kkbank.utils.TransactionUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public BankResponse createAccount(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .transactionInfo(null)
                    .build();
        }
        User newUser = AccountUtils.buildUser(userRequest);

        User savedUser = userRepository.save(newUser);
        String savedUserName = AccountUtils.deriveAccountName(savedUser);
        if (emailService != null) {
            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(savedUser.getEmail())
                    .messageBody("Congratulations! Your Account has been successfully created.\n\nAccount Details : \n"
                            + "Account Name : "
                            + savedUserName
                            + "\nAccount Number : "
                            + savedUser.getAccountNumber())
                    .subject("Account Creation")
                    .build();
            emailService.sendEmailAlert(emailDetails);
        }
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountName(savedUserName)
                        .build())
                .transactionInfo(null)
                .build();
    }

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest request) {
        if (userRepository.existsByAccountNumber(request.getAccountNumber())) {
            User user = userRepository.findByAccountNumber(request.getAccountNumber());
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountBalance(user.getAccountBalance())
                            .accountNumber(request.getAccountNumber())
                            .accountName(AccountUtils.deriveAccountName(user))
                            .build())
                    .transactionInfo(null)
                    .build();
        }
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                .accountInfo(null)
                .transactionInfo(null)
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest request) {
        if (userRepository.existsByAccountNumber(request.getAccountNumber())) {
            User user = userRepository.findByAccountNumber(request.getAccountNumber());
            return AccountUtils.deriveAccountName(user);
        }
        return AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE;
    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest request) {
        if (userRepository.existsByAccountNumber(request.getAccountNumber())) {
            User userToCredit = userRepository.findByAccountNumber(request.getAccountNumber());
            userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(request.getAmount()));
            userRepository.save(userToCredit);

            Transaction creditTransaction = Transaction.builder()
                    .accountNumber(userToCredit.getAccountNumber())
                    .creditAmount(request.getAmount())
                    .debitAmount(BigDecimal.ZERO)
                    .transactionType(TransactionUtils.CREDIT)
                    .transactionDescription(TransactionUtils.createCreditDescription())
                    .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                    .build();
            Transaction savedTransaction = transactionRepository.save(creditTransaction);

            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_CREDIT_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_CREDIT_SUCCESS_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountName(AccountUtils.deriveAccountName(userToCredit))
                            .accountBalance(userToCredit.getAccountBalance())
                            .accountNumber(userToCredit.getAccountNumber())
                            .build())
                    .transactionInfo(TransactionInfo.builder()
                            .transactionId(savedTransaction.getTransactionID())
                            .transactionAmount(savedTransaction.getCreditAmount())
                            .transactionType(savedTransaction.getTransactionType())
                            .build())
                    .build();
        }
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                .accountInfo(null)
                .transactionInfo(null)
                .build();
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest request) {

        if (!(userRepository.existsByAccountNumber(request.getAccountNumber()))) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .transactionInfo(null)
                    .build();
        }

        User userToDebit = userRepository.findByAccountNumber(request.getAccountNumber());
        BigDecimal currentBalance = userToDebit.getAccountBalance();

        if (currentBalance.compareTo(request.getAmount()) < 0) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_DEBIT_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_DEBIT_FAILURE_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountName(AccountUtils.deriveAccountName(userToDebit))
                            .accountBalance(userToDebit.getAccountBalance())
                            .accountNumber(userToDebit.getAccountNumber())
                            .build())
                    .transactionInfo(null)
                    .build();
        }

        userToDebit.setAccountBalance(currentBalance.subtract(request.getAmount()));
        userRepository.save(userToDebit);
        Transaction debitTransaction = Transaction.builder()
                .transactionType(TransactionUtils.DEBIT)
                .transactionDescription(TransactionUtils.createDebitDescription())
                .accountNumber(userToDebit.getAccountNumber())
                .creditAmount(BigDecimal.ZERO)
                .debitAmount(request.getAmount())
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .build();
        Transaction savedTransaction = transactionRepository.save(debitTransaction);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_DEBIT_CODE)
                .responseMessage(AccountUtils.ACCOUNT_DEBIT_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(AccountUtils.deriveAccountName(userToDebit))
                        .accountBalance(userToDebit.getAccountBalance())
                        .accountNumber(userToDebit.getAccountNumber())
                        .build())
                .transactionInfo(TransactionInfo.builder()
                        .transactionId(savedTransaction.getTransactionID())
                        .transactionAmount(savedTransaction.getDebitAmount())
                        .transactionType(savedTransaction.getTransactionType())
                        .build())
                .build();
    }

    @Override
    public BankResponse transfer(TransferRequest request) {

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.TRANSFER_FAILURE_CODE)
                    .responseMessage(AccountUtils.INVALID_AMOUNT)
                    .accountInfo(null)
                    .transactionInfo(null)
                    .build();
        }

        Boolean isSourceAccountExist = userRepository.existsByAccountNumber(request.getSourceAccountNumber());
        Boolean isDestinationAccountExist = userRepository.existsByAccountNumber(request.getDestinationAccountNumber());

        if (!(isSourceAccountExist && isDestinationAccountExist)) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.TRANSFER_FAILURE_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE + ". "
                            + AccountUtils.TRANSFER_FAILURE_MESSAGE)
                    .accountInfo(null)
                    .transactionInfo(null)
                    .build();
        }

        User sourceUser = userRepository.findByAccountNumber(request.getSourceAccountNumber());
        if (sourceUser.getAccountBalance().compareTo(request.getAmount()) < 0) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.TRANSFER_FAILURE_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_DEBIT_FAILURE_MESSAGE + ". "
                            + AccountUtils.TRANSFER_FAILURE_MESSAGE)
                    .accountInfo(null)
                    .transactionInfo(null)
                    .build();
        }

        User destinationUser = userRepository.findByAccountNumber(request.getDestinationAccountNumber());

        // Debit from source account
        sourceUser.setAccountBalance(sourceUser.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(sourceUser);
        transactionRepository.save(Transaction.builder()
                        .transactionDescription(TransactionUtils.createTransferDescription(destinationUser.getAccountNumber(), false))
                        .debitAmount(request.getAmount())
                        .transactionType(TransactionUtils.TRANSFER)
                        .creditAmount(BigDecimal.ZERO)
                        .accountNumber(sourceUser.getAccountNumber())
                        .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                        .build());

        // Credit to destination account
        destinationUser.setAccountBalance(destinationUser.getAccountBalance().add(request.getAmount()));
        userRepository.save(destinationUser);
        Transaction savedCreditTransaction = transactionRepository.save(Transaction.builder()
                .transactionDescription(TransactionUtils.createTransferDescription(sourceUser.getAccountNumber(), true))
                .creditAmount(request.getAmount())
                .transactionType(TransactionUtils.TRANSFER)
                .debitAmount(BigDecimal.ZERO)
                .accountNumber(destinationUser.getAccountNumber())
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .build());

        if (emailService != null) {
            EmailDetails sourceEmailDetails = EmailDetails.builder()
                    .subject("DEBIT ALERT")
                    .recipient(sourceUser.getEmail())
                    .messageBody("Your transfer of "
                            + request.getAmount()
                            + " rupees to the account "
                            + request.getDestinationAccountNumber()
                            + " ("
                            + AccountUtils.deriveAccountName(destinationUser)
                            + ") is successful."
                            + "\nYour current balance : "
                            + sourceUser.getAccountBalance())
                    .build();
            emailService.sendEmailAlert(sourceEmailDetails);

            EmailDetails destinationEmailDetails = EmailDetails.builder()
                    .recipient(destinationUser.getEmail())
                    .subject("CREDIT ALERT")
                    .messageBody("Amount Credited : "
                            + request.getAmount()
                            + "\nTransferred by : "
                            + sourceUser.getAccountNumber()
                            + "("
                            + AccountUtils.deriveAccountName(sourceUser)
                            + ")"
                            + "\nYour current balance : "
                            + destinationUser.getAccountBalance())
                    .build();
            emailService.sendEmailAlert(destinationEmailDetails);
        }

        return BankResponse.builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESS_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESS_MESSAGE)
                .accountInfo(null)
                .transactionInfo(TransactionInfo.builder()
                        .transactionId(savedCreditTransaction.getTransactionID())
                        .transactionAmount(savedCreditTransaction.getCreditAmount())
                        .transactionType(savedCreditTransaction.getTransactionType())
                        .build())
                .build();
    }

}
