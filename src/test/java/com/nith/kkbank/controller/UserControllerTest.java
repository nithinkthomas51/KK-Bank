package com.nith.kkbank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nith.kkbank.dto.*;
import com.nith.kkbank.entity.User;
import com.nith.kkbank.service.UserService;
import com.nith.kkbank.utils.AccountUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    UserRequest userRequest;
    User user;
    EnquiryRequest enquiryRequest;
    CreditDebitRequest creditDebitRequest;
    AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        userRequest = new UserRequest("Abc", "Def", "Ghi", "Male", "Jkl, Mno",
                "KRL", "abcdef07@gmail.com", "1234567890", "9876543210");
        user = AccountUtils.buildUser(userRequest);
        enquiryRequest = new EnquiryRequest(user.getAccountNumber());
        creditDebitRequest = new CreditDebitRequest(user.getAccountNumber(), BigDecimal.valueOf(1000));
    }

    @AfterEach
    void tearDown() throws Exception{
        autoCloseable.close();
    }

    @Test
    void testCreateAccountNew() throws Exception {
        BankResponse createSuccessResponse = BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(user.getAccountNumber())
                        .accountBalance(user.getAccountBalance())
                        .accountName(AccountUtils.deriveAccountName(user))
                        .build())
                .build();
        when(userService.createAccount(userRequest)).thenReturn(createSuccessResponse);
        mockMvc.perform(post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testBalanceEnquiry() throws Exception{
        BankResponse balanceResponse = BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(user.getAccountBalance())
                        .accountNumber(user.getAccountNumber())
                        .accountName(AccountUtils.deriveAccountName(user))
                        .build())
                .build();
        when(userService.balanceEnquiry(enquiryRequest)).thenReturn(balanceResponse);
        mockMvc.perform(get("/user/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enquiryRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testNameEnquiry() throws Exception{
        when(userService.nameEnquiry(enquiryRequest)).thenReturn(AccountUtils.deriveAccountName(user));
        mockMvc.perform(get("/user/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enquiryRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testCreditAccount() throws Exception{
        BankResponse creditResponse = BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDIT_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREDIT_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(AccountUtils.deriveAccountName(user))
                        .accountBalance(user.getAccountBalance().add(creditDebitRequest.getAmount()))
                        .accountNumber(user.getAccountNumber())
                        .build())
                .build();
        when(userService.creditAccount(creditDebitRequest)).thenReturn(creditResponse);
        mockMvc.perform(post("/user/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditDebitRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value(AccountUtils.ACCOUNT_CREDIT_CODE))
                .andExpect(jsonPath("$.responseMessage").value(AccountUtils.ACCOUNT_CREDIT_SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.accountInfo.accountBalance")
                        .value(user.getAccountBalance().add(creditDebitRequest.getAmount())));
    }

    @Test
    void testDebitAccount() throws Exception{
        user.setAccountBalance(BigDecimal.valueOf(10000));
        BankResponse debitResponse = BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_DEBIT_CODE)
                .responseMessage(AccountUtils.ACCOUNT_DEBIT_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(AccountUtils.deriveAccountName(user))
                        .accountBalance(user.getAccountBalance().subtract(creditDebitRequest.getAmount()))
                        .accountNumber(user.getAccountNumber())
                        .build())
                .build();
        when(userService.debitAccount(creditDebitRequest)).thenReturn(debitResponse);
        mockMvc.perform(post("/user/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creditDebitRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value(AccountUtils.ACCOUNT_DEBIT_CODE))
                .andExpect(jsonPath("$.responseMessage").value(AccountUtils.ACCOUNT_DEBIT_SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.accountInfo.accountBalance")
                        .value(user.getAccountBalance().subtract(creditDebitRequest.getAmount())));
    }

    // TODO : Test case for /user/transfer endpoint
}