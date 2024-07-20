package com.nith.kkbank.service.impl;

import com.nith.kkbank.dto.*;
import com.nith.kkbank.entity.Transaction;
import com.nith.kkbank.entity.User;
import com.nith.kkbank.repository.TransactionRepository;
import com.nith.kkbank.repository.UserRepository;
import com.nith.kkbank.service.UserService;
import com.nith.kkbank.utils.AccountUtils;
import com.nith.kkbank.utils.TransactionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    private UserService userService;
    AutoCloseable autoCloseable;
    UserRequest userRequest;
    User user;
    Transaction transaction;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, null, transactionRepository);
        userRequest = new UserRequest("Abc", "Def", "Ghi", "Male", "Jkl, Mno",
                "KRL", "abcdef07@gmail.com", "1234567890", "9876543210");
    }

    @AfterEach
    void tearDown() throws Exception{
        autoCloseable.close();
    }

    @Test
    void testCreateAccount_new() {
        mock(UserRepository.class);
        user = AccountUtils.buildUser(userRequest);
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        BankResponse createResponse = userService.createAccount(userRequest);
        assertThat(createResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE);
        assertThat(createResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE);
        assertThat(createResponse.getAccountInfo().getAccountNumber().length()).isEqualTo(10);
        assertThat(createResponse.getAccountInfo().getAccountNumber().matches("^2024.*")).isTrue();
    }

    @Test
    void testCreateAccount_exists() {
        mock(UserRepository.class);
        user = AccountUtils.buildUser(userRequest);
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        BankResponse createResponse = userService.createAccount(userRequest);
        assertThat(createResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_EXISTS_CODE);
        assertThat(createResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_EXISTS_MESSAGE);
        assertThat(createResponse.getAccountInfo()).isNull();
    }

    @Test
    void testBalanceEnquiryOnExistingAccountWithZeroBalance() {
        mock(UserRepository.class);
        user = AccountUtils.buildUser(userRequest);
        when(userRepository.existsByAccountNumber(user.getAccountNumber())).thenReturn(true);
        when(userRepository.findByAccountNumber(user.getAccountNumber())).thenReturn(user);
        BankResponse balanceResponse = userService.balanceEnquiry(new EnquiryRequest(user.getAccountNumber()));
        assertThat(balanceResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_FOUND_CODE);
        assertThat(balanceResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_FOUND_MESSAGE);
        assertThat(balanceResponse.getAccountInfo()).isNotNull();
        assertThat(balanceResponse.getAccountInfo().getAccountBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(balanceResponse.getAccountInfo().getAccountNumber()).isEqualTo(user.getAccountNumber());
        assertThat(balanceResponse.getAccountInfo().getAccountName()).isEqualTo(AccountUtils.deriveAccountName(user));
    }

    @Test
    void testBalanceEnquiryOnExistingAccountWithNonZeroBalance() {
        mock(UserRepository.class);
        user = AccountUtils.buildUser(userRequest);
        user.setAccountBalance(BigDecimal.valueOf(10000));
        when(userRepository.existsByAccountNumber(user.getAccountNumber())).thenReturn(true);
        when(userRepository.findByAccountNumber(user.getAccountNumber())).thenReturn(user);
        BankResponse balanceResponse = userService.balanceEnquiry(new EnquiryRequest(user.getAccountNumber()));
        assertThat(balanceResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_FOUND_CODE);
        assertThat(balanceResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_FOUND_MESSAGE);
        assertThat(balanceResponse.getAccountInfo()).isNotNull();
        assertThat(balanceResponse.getAccountInfo().getAccountBalance()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(balanceResponse.getAccountInfo().getAccountNumber()).isEqualTo(user.getAccountNumber());
        assertThat(balanceResponse.getAccountInfo().getAccountName()).isEqualTo(AccountUtils.deriveAccountName(user));
    }

    @Test
    void testBalanceEnquiryOnNonExistingAccount() {
        mock(UserRepository.class);
        when(userRepository.existsByAccountNumber(Mockito.any(String.class))).thenReturn(false);
        BankResponse balanceResponse = userService.balanceEnquiry(new EnquiryRequest("2024567890"));
        assertThat(balanceResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_CODE);
        assertThat(balanceResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE);
        assertThat(balanceResponse.getAccountInfo()).isNull();
    }

    @Test
    void testNameEnquiryOnExistingAccount() {
        mock(UserRepository.class);
        user = AccountUtils.buildUser(userRequest);
        when(userRepository.existsByAccountNumber(user.getAccountNumber())).thenReturn(true);
        when(userRepository.findByAccountNumber(user.getAccountNumber())).thenReturn(user);
        String accountName = userService.nameEnquiry(new EnquiryRequest(user.getAccountNumber()));
        assertThat(accountName).isEqualTo(AccountUtils.deriveAccountName(user));
    }

    @Test
    void testNameEnquiryOnNonExistingAccount() {
        mock(UserRepository.class);
        when(userRepository.existsByAccountNumber(Mockito.any(String.class))).thenReturn(false);
        String accountName = userService.nameEnquiry(new EnquiryRequest("2024567890"));
        assertThat(accountName).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE);
    }

    @Test
    void testCreditAccountOnExistingAccount() {
        mock(UserRepository.class);
        mock(TransactionRepository.class);
        user = AccountUtils.buildUser(userRequest);
        when(userRepository.existsByAccountNumber(user.getAccountNumber())).thenReturn(true);
        when(userRepository.findByAccountNumber(user.getAccountNumber())).thenReturn(user);
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        CreditDebitRequest creditDebitRequest = new CreditDebitRequest(user.getAccountNumber(), BigDecimal.valueOf(5000));
        transaction = Transaction.builder()
                .transactionType(TransactionUtils.CREDIT)
                .transactionDescription(TransactionUtils.createCreditDescription())
                .accountNumber(user.getAccountNumber())
                .creditAmount(creditDebitRequest.getAmount())
                .debitAmount(BigDecimal.ZERO)
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .build();
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        BankResponse creditResponse = userService.creditAccount(creditDebitRequest);
        assertThat(creditResponse.getAccountInfo()).isNotNull();
        assertThat(creditResponse.getAccountInfo().getAccountBalance()).isEqualTo(creditDebitRequest.getAmount());
        assertThat(creditResponse.getAccountInfo().getAccountName()).isEqualTo(AccountUtils.deriveAccountName(user));
        assertThat(creditResponse.getAccountInfo().getAccountNumber()).isEqualTo(creditDebitRequest.getAccountNumber());
        assertThat(creditResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_CREDIT_CODE);
        assertThat(creditResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_CREDIT_SUCCESS_MESSAGE);
        assertThat(creditResponse.getTransactionInfo()).isNotNull();
        assertThat(creditResponse.getTransactionInfo().getTransactionType()).isEqualTo(transaction.getTransactionType());
        assertThat(creditResponse.getTransactionInfo().getTransactionAmount()).isEqualTo(transaction.getCreditAmount());
    }

    @Test
    void testCreditAccountOnNonExistingAccount() {
        mock(UserRepository.class);
        when(userRepository.existsByAccountNumber(Mockito.any(String.class))).thenReturn(false);
        CreditDebitRequest creditDebitRequest = new CreditDebitRequest("2024567890", BigDecimal.valueOf(5000));
        BankResponse creditResponse = userService.creditAccount(creditDebitRequest);
        assertThat(creditResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_CODE);
        assertThat(creditResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE);
        assertThat(creditResponse.getAccountInfo()).isNull();
        assertThat(creditResponse.getTransactionInfo()).isNull();
    }

    @Test
    void testDebitAccountOnExistingAccountWithNonZeroBalance() {
        mock(UserRepository.class);
        mock(TransactionRepository.class);
        user = AccountUtils.buildUser(userRequest);
        user.setAccountBalance(BigDecimal.valueOf(20000));
        BigDecimal currentBalance = user.getAccountBalance();
        when(userRepository.existsByAccountNumber(user.getAccountNumber())).thenReturn(true);
        when(userRepository.findByAccountNumber(user.getAccountNumber())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        CreditDebitRequest debitRequest = new CreditDebitRequest(user.getAccountNumber(), BigDecimal.valueOf(10000));
        transaction = Transaction.builder()
                .transactionDescription(TransactionUtils.createDebitDescription())
                .transactionType(TransactionUtils.DEBIT)
                .debitAmount(debitRequest.getAmount())
                .creditAmount(BigDecimal.ZERO)
                .accountNumber(debitRequest.getAccountNumber())
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .build();
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        BankResponse debitResponse = userService.debitAccount(debitRequest);
        assertThat(debitResponse.getAccountInfo()).isNotNull();
        assertThat(debitResponse.getAccountInfo().getAccountBalance())
                .isEqualTo(currentBalance.subtract(debitRequest.getAmount()));
        assertThat(debitResponse.getAccountInfo().getAccountName()).isEqualTo(AccountUtils.deriveAccountName(user));
        assertThat(debitResponse.getAccountInfo().getAccountNumber()).isEqualTo(debitRequest.getAccountNumber());
        assertThat(debitResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_DEBIT_CODE);
        assertThat(debitResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_DEBIT_SUCCESS_MESSAGE);
        assertThat(debitResponse.getTransactionInfo()).isNotNull();
        assertThat(debitResponse.getTransactionInfo().getTransactionAmount()).isEqualTo(transaction.getDebitAmount());
        assertThat(debitResponse.getTransactionInfo().getTransactionType()).isEqualTo(transaction.getTransactionType());
    }

    @Test
    void testDebitAccountOnExistingAccountWithNonZeroNotEnoughBalance() {
        mock(UserRepository.class);
        user = AccountUtils.buildUser(userRequest);
        user.setAccountBalance(BigDecimal.valueOf(20000));
        when(userRepository.existsByAccountNumber(user.getAccountNumber())).thenReturn(true);
        when(userRepository.findByAccountNumber(user.getAccountNumber())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        CreditDebitRequest debitRequest = new CreditDebitRequest(user.getAccountNumber(), BigDecimal.valueOf(30000));
        BankResponse debitResponse = userService.debitAccount(debitRequest);
        assertThat(debitResponse.getAccountInfo()).isNotNull();
        assertThat(debitResponse.getAccountInfo().getAccountBalance()).isEqualTo(user.getAccountBalance());
        assertThat(debitResponse.getAccountInfo().getAccountName()).isEqualTo(AccountUtils.deriveAccountName(user));
        assertThat(debitResponse.getAccountInfo().getAccountNumber()).isEqualTo(debitRequest.getAccountNumber());
        assertThat(debitResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_DEBIT_CODE);
        assertThat(debitResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_DEBIT_FAILURE_MESSAGE);
        assertThat(debitResponse.getTransactionInfo()).isNull();
    }

    @Test
    void testDebitAccountOnExistingAccountWithZeroBalance() {
        mock(UserRepository.class);
        user = AccountUtils.buildUser(userRequest);
        when(userRepository.existsByAccountNumber(user.getAccountNumber())).thenReturn(true);
        when(userRepository.findByAccountNumber(user.getAccountNumber())).thenReturn(user);
        CreditDebitRequest debitRequest = new CreditDebitRequest(user.getAccountNumber(), BigDecimal.valueOf(10000));
        BankResponse debitResponse = userService.debitAccount(debitRequest);
        assertThat(debitResponse.getAccountInfo()).isNotNull();
        assertThat(debitResponse.getAccountInfo().getAccountBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(debitResponse.getAccountInfo().getAccountName()).isEqualTo(AccountUtils.deriveAccountName(user));
        assertThat(debitResponse.getAccountInfo().getAccountNumber()).isEqualTo(debitRequest.getAccountNumber());
        assertThat(debitResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_DEBIT_CODE);
        assertThat(debitResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_DEBIT_FAILURE_MESSAGE);
        assertThat(debitResponse.getTransactionInfo()).isNull();
    }

    @Test
    void testDebitAccountOnNonExistingAccount() {
        mock(UserRepository.class);
        when(userRepository.existsByAccountNumber(Mockito.any(String.class))).thenReturn(false);
        CreditDebitRequest debitRequest = new CreditDebitRequest("2024567890", BigDecimal.valueOf(1000));
        BankResponse debitResponse = userService.debitAccount(debitRequest);
        assertThat(debitResponse.getAccountInfo()).isNull();
        assertThat(debitResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_CODE);
        assertThat(debitResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE);
        assertThat(debitResponse.getTransactionInfo()).isNull();
    }

    @Test
    void testTransferToZeroBalanceAccount() {
        mock(UserRepository.class);
        User user1 = AccountUtils.buildUser(userRequest);
        user1.setAccountBalance(BigDecimal.valueOf(10000));
        BigDecimal sourceAccountBalance = user1.getAccountBalance();
        UserRequest additionalUserRequest = new UserRequest("JKL", "MNO", "PQR", "Male", "Stu, Vwx",
                "NGA", "jklmno07@gmail.com", "9897965432", "9988776655");
        User user2 = AccountUtils.buildUser(additionalUserRequest);
        BigDecimal destinationAccountBalance = user2.getAccountBalance();
        when(userRepository.existsByAccountNumber(Mockito.any(String.class))).thenReturn(true);
        when(userRepository.findByAccountNumber(user1.getAccountNumber())).thenReturn(user1);
        when(userRepository.findByAccountNumber(user2.getAccountNumber())).thenReturn(user2);
        when(userRepository.save(user1)).thenReturn(user1);
        when(userRepository.save(user2)).thenReturn(user2);
        TransferRequest transferRequest = TransferRequest.builder()
                .sourceAccountNumber(user1.getAccountNumber())
                .destinationAccountNumber(user2.getAccountNumber())
                .amount(BigDecimal.valueOf(5000))
                .build();
        transaction = Transaction.builder()
                .transactionDescription(TransactionUtils.createTransferDescription(user2.getAccountNumber(), true))
                .transactionType(TransactionUtils.TRANSFER)
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .accountNumber(transferRequest.getDestinationAccountNumber())
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(transferRequest.getAmount())
                .build();
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        BankResponse transferResponse = userService.transfer(transferRequest);
        assertThat(transferResponse.getResponseCode()).isEqualTo(AccountUtils.TRANSFER_SUCCESS_CODE);
        assertThat(transferResponse.getResponseMessage()).isEqualTo(AccountUtils.TRANSFER_SUCCESS_MESSAGE);
        assertThat(user1.getAccountBalance()).isEqualTo(sourceAccountBalance.subtract(transferRequest.getAmount()));
        assertThat(user2.getAccountBalance()).isEqualTo(destinationAccountBalance.add(transferRequest.getAmount()));
        assertThat(transferResponse.getTransactionInfo()).isNotNull();
        assertThat(transferResponse.getTransactionInfo().getTransactionAmount()).isEqualTo(transferRequest.getAmount());
        assertThat(transferResponse.getTransactionInfo().getTransactionType()).isEqualTo(TransactionUtils.TRANSFER);
    }

    @Test
    void testTransferToNonZeroBalanceAccount() {
        mock(UserRepository.class);
        mock(TransactionRepository.class);
        User user1 = AccountUtils.buildUser(userRequest);
        user1.setAccountBalance(BigDecimal.valueOf(10000));
        BigDecimal sourceAccountBalance = user1.getAccountBalance();
        UserRequest additionalUserRequest = new UserRequest("JKL", "MNO", "PQR", "Male", "Stu, Vwx",
                "NGA", "jklmno07@gmail.com", "9897965432", "9988776655");
        User user2 = AccountUtils.buildUser(additionalUserRequest);
        user2.setAccountBalance(BigDecimal.valueOf(7000));
        BigDecimal destinationAccountBalance = user2.getAccountBalance();
        when(userRepository.existsByAccountNumber(Mockito.any(String.class))).thenReturn(true);
        when(userRepository.findByAccountNumber(user1.getAccountNumber())).thenReturn(user1);
        when(userRepository.findByAccountNumber(user2.getAccountNumber())).thenReturn(user2);
        when(userRepository.save(user1)).thenReturn(user1);
        when(userRepository.save(user2)).thenReturn(user2);
        TransferRequest transferRequest = TransferRequest.builder()
                .sourceAccountNumber(user1.getAccountNumber())
                .destinationAccountNumber(user2.getAccountNumber())
                .amount(BigDecimal.valueOf(5000))
                .build();
        transaction = Transaction.builder()
                .transactionStatus(TransactionUtils.TRANSACTION_COMPLETE)
                .accountNumber(user2.getAccountNumber())
                .creditAmount(transferRequest.getAmount())
                .debitAmount(BigDecimal.ZERO)
                .transactionType(TransactionUtils.TRANSFER)
                .transactionDescription(TransactionUtils.createTransferDescription(user2.getAccountNumber(), true))
                .build();
        when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        BankResponse transferResponse = userService.transfer(transferRequest);
        assertThat(transferResponse.getResponseCode()).isEqualTo(AccountUtils.TRANSFER_SUCCESS_CODE);
        assertThat(transferResponse.getResponseMessage()).isEqualTo(AccountUtils.TRANSFER_SUCCESS_MESSAGE);
        assertThat(user1.getAccountBalance()).isEqualTo(sourceAccountBalance.subtract(transferRequest.getAmount()));
        assertThat(user2.getAccountBalance()).isEqualTo(destinationAccountBalance.add(transferRequest.getAmount()));
        assertThat(transferResponse.getTransactionInfo()).isNotNull();
        assertThat(transferResponse.getTransactionInfo().getTransactionType()).isEqualTo(TransactionUtils.TRANSFER);
        assertThat(transferResponse.getTransactionInfo().getTransactionAmount()).isEqualTo(transferRequest.getAmount());
    }

    @Test
    void testTransferBothAccountNotExist() {
        mock(UserRepository.class);
        when(userRepository.existsByAccountNumber(Mockito.any(String.class))).thenReturn(false);
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber("2024567890")
                .destinationAccountNumber("2024123456")
                .amount(BigDecimal.valueOf(3000))
                .build();
        BankResponse transferResponse = userService.transfer(request);
        assertThat(transferResponse.getResponseCode()).isEqualTo(AccountUtils.TRANSFER_FAILURE_CODE);
        assertThat(transferResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE
                + ". " + AccountUtils.TRANSFER_FAILURE_MESSAGE);
        assertThat(transferResponse.getAccountInfo()).isNull();
        assertThat(transferResponse.getTransactionInfo()).isNull();
    }

    @Test
    void testTransferSourceAccountNotExist() {
        mock(UserRepository.class);
        User destinationUser = AccountUtils.buildUser(userRequest);
        UserRequest additionalUser = new UserRequest("JKL", "MNO", "PQR", "Male", "Stu, Vwx",
                "NGA", "jklmno07@gmail.com", "9897965432", "9988776655");
        User sourceUser = AccountUtils.buildUser(additionalUser);
        when(userRepository.existsByAccountNumber(sourceUser.getAccountNumber())).thenReturn(false);
        when(userRepository.existsByAccountNumber(destinationUser.getAccountNumber())).thenReturn(true);
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber(sourceUser.getAccountNumber())
                .destinationAccountNumber(destinationUser.getAccountNumber())
                .amount(BigDecimal.valueOf(3000))
                .build();
        BankResponse transferResponse = userService.transfer(request);
        assertThat(transferResponse.getResponseCode()).isEqualTo(AccountUtils.TRANSFER_FAILURE_CODE);
        assertThat(transferResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE
                + ". " + AccountUtils.TRANSFER_FAILURE_MESSAGE);
        assertThat(transferResponse.getTransactionInfo()).isNull();
        assertThat(transferResponse.getAccountInfo()).isNull();
    }

    @Test
    void testTransferDestinationAccountNotExist() {
        mock(UserRepository.class);
        User destinationUser = AccountUtils.buildUser(userRequest);
        UserRequest additionalUser = new UserRequest("JKL", "MNO", "PQR", "Male", "Stu, Vwx",
                "NGA", "jklmno07@gmail.com", "9897965432", "9988776655");
        User sourceUser = AccountUtils.buildUser(additionalUser);
        when(userRepository.existsByAccountNumber(sourceUser.getAccountNumber())).thenReturn(true);
        when(userRepository.existsByAccountNumber(destinationUser.getAccountNumber())).thenReturn(false);
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber(sourceUser.getAccountNumber())
                .destinationAccountNumber(destinationUser.getAccountNumber())
                .amount(BigDecimal.valueOf(3000))
                .build();
        BankResponse transferResponse = userService.transfer(request);
        assertThat(transferResponse.getResponseCode()).isEqualTo(AccountUtils.TRANSFER_FAILURE_CODE);
        assertThat(transferResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE
                + ". " + AccountUtils.TRANSFER_FAILURE_MESSAGE);
        assertThat(transferResponse.getAccountInfo()).isNull();
        assertThat(transferResponse.getTransactionInfo()).isNull();
    }

    @Test
    void testTransferFromInsufficientBalanceSourceAccount() {
        mock(UserRepository.class);
        User sourceUser = AccountUtils.buildUser(userRequest);
        sourceUser.setAccountBalance(BigDecimal.valueOf(1000));
        when(userRepository.existsByAccountNumber(Mockito.any(String.class))).thenReturn(true);
        when(userRepository.findByAccountNumber(sourceUser.getAccountNumber())).thenReturn(sourceUser);
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber(sourceUser.getAccountNumber())
                .destinationAccountNumber("2024123456")
                .amount(BigDecimal.valueOf(3000))
                .build();
        BankResponse transferResponse = userService.transfer(request);
        assertThat(transferResponse.getResponseCode()).isEqualTo(AccountUtils.TRANSFER_FAILURE_CODE);
        assertThat(transferResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_DEBIT_FAILURE_MESSAGE
                + ". " + AccountUtils.TRANSFER_FAILURE_MESSAGE);
        assertThat(transferResponse.getTransactionInfo()).isNull();
        assertThat(transferResponse.getAccountInfo()).isNull();
    }

    @Test
    void testTransferZeroAmount() {
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber("2024567890")
                .destinationAccountNumber("2024123456")
                .amount(BigDecimal.ZERO)
                .build();
        BankResponse transferResponse = userService.transfer(request);
        assertThat(transferResponse.getResponseCode()).isEqualTo(AccountUtils.TRANSFER_FAILURE_CODE);
        assertThat(transferResponse.getResponseMessage()).isEqualTo(AccountUtils.INVALID_AMOUNT);
        assertThat(transferResponse.getTransactionInfo()).isNull();
    }

    @Test
    void testTransferNegativeAmount() {
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber("2024567890")
                .destinationAccountNumber("2024123456")
                .amount(BigDecimal.valueOf(-1000))
                .build();
        BankResponse transferResponse = userService.transfer(request);
        assertThat(transferResponse.getResponseCode()).isEqualTo(AccountUtils.TRANSFER_FAILURE_CODE);
        assertThat(transferResponse.getResponseMessage()).isEqualTo(AccountUtils.INVALID_AMOUNT);
        assertThat(transferResponse.getTransactionInfo()).isNull();
    }
}