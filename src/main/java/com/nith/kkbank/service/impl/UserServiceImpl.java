package com.nith.kkbank.service.impl;
import com.nith.kkbank.dto.*;
import com.nith.kkbank.entity.User;
import com.nith.kkbank.repository.UserRepository;
import com.nith.kkbank.service.EmailService;
import com.nith.kkbank.service.UserService;
import com.nith.kkbank.utils.AccountUtils;
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

    @Override
    public BankResponse createAccount(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
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
                    .build();
        }
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                .accountInfo(null)
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

            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_CREDIT_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_CREDIT_SUCCESS_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountName(AccountUtils.deriveAccountName(userToCredit))
                            .accountBalance(userToCredit.getAccountBalance())
                            .accountNumber(userToCredit.getAccountNumber())
                            .build())
                    .build();
        }
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                .accountInfo(null)
                .build();
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest request) {

        if (!(userRepository.existsByAccountNumber(request.getAccountNumber()))) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
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
                    .build();
        }

        userToDebit.setAccountBalance(currentBalance.subtract(request.getAmount()));
        userRepository.save(userToDebit);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_DEBIT_CODE)
                .responseMessage(AccountUtils.ACCOUNT_DEBIT_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(AccountUtils.deriveAccountName(userToDebit))
                        .accountBalance(userToDebit.getAccountBalance())
                        .accountNumber(userToDebit.getAccountNumber())
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
                    .build();
        }

        User sourceUser = userRepository.findByAccountNumber(request.getSourceAccountNumber());
        if (sourceUser.getAccountBalance().compareTo(request.getAmount()) < 0) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.TRANSFER_FAILURE_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_DEBIT_FAILURE_MESSAGE + ". "
                            + AccountUtils.TRANSFER_FAILURE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User destinationUser = userRepository.findByAccountNumber(request.getDestinationAccountNumber());

        // Debit from source account
        sourceUser.setAccountBalance(sourceUser.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(sourceUser);

        // Credit to destination account
        destinationUser.setAccountBalance(destinationUser.getAccountBalance().add(request.getAmount()));
        userRepository.save(destinationUser);

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
                .build();
    }

}
