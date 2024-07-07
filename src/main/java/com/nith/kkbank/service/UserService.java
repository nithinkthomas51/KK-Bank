package com.nith.kkbank.service;

import com.nith.kkbank.dto.BankResponse;
import com.nith.kkbank.dto.EnquiryRequest;
import com.nith.kkbank.dto.UserRequest;

public interface UserService {

    BankResponse createAccount(UserRequest userRequest);
    BankResponse balanceEnquiry(EnquiryRequest request);
    String nameEnquiry(EnquiryRequest request);
}
