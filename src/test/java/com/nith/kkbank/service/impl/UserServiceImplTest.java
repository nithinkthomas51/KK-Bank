package com.nith.kkbank.service.impl;

import com.nith.kkbank.dto.BankResponse;
import com.nith.kkbank.dto.CreditDebitRequest;
import com.nith.kkbank.dto.EnquiryRequest;
import com.nith.kkbank.dto.UserRequest;
import com.nith.kkbank.entity.User;
import com.nith.kkbank.repository.UserRepository;
import com.nith.kkbank.service.UserService;
import com.nith.kkbank.utils.AccountUtils;
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
    private UserService userService;
    AutoCloseable autoCloseable;
    UserRequest userRequest;
    User user;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, null);
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
        user = AccountUtils.buildUser(userRequest);
        when(userRepository.existsByAccountNumber(user.getAccountNumber())).thenReturn(true);
        when(userRepository.findByAccountNumber(user.getAccountNumber())).thenReturn(user);
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        CreditDebitRequest creditDebitRequest = new CreditDebitRequest(user.getAccountNumber(), BigDecimal.valueOf(5000));
        BankResponse creditResponse = userService.creditAccount(creditDebitRequest);
        assertThat(creditResponse.getAccountInfo()).isNotNull();
        assertThat(creditResponse.getAccountInfo().getAccountBalance()).isEqualTo(creditDebitRequest.getAmount());
        assertThat(creditResponse.getAccountInfo().getAccountName()).isEqualTo(AccountUtils.deriveAccountName(user));
        assertThat(creditResponse.getAccountInfo().getAccountNumber()).isEqualTo(creditDebitRequest.getAccountNumber());
        assertThat(creditResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_CREDIT_CODE);
        assertThat(creditResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_CREDIT_SUCCESS_MESSAGE);
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
    }

    @Test
    void testDebitAccountOnExistingAccountWithNonZeroBalance() {
        mock(UserRepository.class);
        user = AccountUtils.buildUser(userRequest);
        user.setAccountBalance(BigDecimal.valueOf(20000));
        BigDecimal currentBalance = user.getAccountBalance();
        when(userRepository.existsByAccountNumber(user.getAccountNumber())).thenReturn(true);
        when(userRepository.findByAccountNumber(user.getAccountNumber())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        CreditDebitRequest debitRequest = new CreditDebitRequest(user.getAccountNumber(), BigDecimal.valueOf(10000));
        BankResponse debitResponse = userService.debitAccount(debitRequest);
        assertThat(debitResponse.getAccountInfo()).isNotNull();
        assertThat(debitResponse.getAccountInfo().getAccountBalance())
                .isEqualTo(currentBalance.subtract(debitRequest.getAmount()));
        assertThat(debitResponse.getAccountInfo().getAccountName()).isEqualTo(AccountUtils.deriveAccountName(user));
        assertThat(debitResponse.getAccountInfo().getAccountNumber()).isEqualTo(debitRequest.getAccountNumber());
        assertThat(debitResponse.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_DEBIT_CODE);
        assertThat(debitResponse.getResponseMessage()).isEqualTo(AccountUtils.ACCOUNT_DEBIT_SUCCESS_MESSAGE);
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
    }
}