package com.nith.kkbank.utils;

import com.nith.kkbank.entity.User;

import java.time.Year;

public class AccountUtils {

    public static final String ACCOUNT_EXISTS_CODE = "001";
    public static final String ACCOUNT_EXISTS_MESSAGE = "This user already has an account created!";
    public static final String ACCOUNT_CREATION_SUCCESS_CODE = "002";
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "Account created successfully!";
    public static final String ACCOUNT_NOT_EXISTS_CODE = "003";
    public static final String ACCOUNT_NOT_EXISTS_MESSAGE = "User with the provided account number doesn't exists!";
    public static final String ACCOUNT_FOUND_CODE = "004";
    public static final String ACCOUNT_FOUND_MESSAGE = "User account found";
    public static final String ACCOUNT_CREDIT_CODE = "005";
    public static final String ACCOUNT_CREDIT_SUCCESS_MESSAGE = "User account credited successfully!";
    public static final String ACCOUNT_DEBIT_CODE = "006";
    public static final String ACCOUNT_DEBIT_SUCCESS_MESSAGE = "User account debited successfully!";
    public static final String ACCOUNT_DEBIT_FAILURE_MESSAGE = "Not enough amount available to withdraw!";

    public static String generateAccountNumber() {
        Year currentYear = Year.now();
        int min = 100000;
        int max = 999999;
        int randomNumber = (int) Math.floor(Math.random() * (max - min + 1) + min);

        String year = String.valueOf(currentYear);
        String randNum = String.valueOf(randomNumber);

        return year + randNum;
    }

    public static String deriveAccountName(User user) {
        return user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName();
    }
}
