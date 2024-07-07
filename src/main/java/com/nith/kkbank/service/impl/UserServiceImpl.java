package com.nith.kkbank.service.impl;
import com.nith.kkbank.dto.*;
import com.nith.kkbank.entity.User;
import com.nith.kkbank.repository.UserRepository;
import com.nith.kkbank.service.EmailService;
import com.nith.kkbank.service.UserService;
import com.nith.kkbank.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
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
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .otherName(userRequest.getOtherName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .phoneNumber(userRequest.getPhoneNumber())
                .alternatePhoneNumber(userRequest.getAlternatePhoneNumber())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(newUser);
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .messageBody("Congratulations! Your Account has been successfully created.\n\nAccount Details : \n"
                            +"Account Name : "
                            + savedUser.getFirstName()
                            + " "
                            + savedUser.getLastName()
                            + " "
                            + savedUser.getOtherName()
                            +"\nAccount Number : "
                            + savedUser.getAccountNumber())
                .subject("Account Creation")
                .build();
        emailService.sendEmailAlert(emailDetails);
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountName(savedUser.getFirstName() + " " + savedUser.getLastName() + " " + savedUser.getOtherName())
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
                            .accountName(user.getFirstName() + " " + user.getLastName())
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
            return user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName();
        }
        return AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE;
    }
}
