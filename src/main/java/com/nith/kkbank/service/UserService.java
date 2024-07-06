package com.nith.kkbank.service;

import com.nith.kkbank.dto.BankResponse;
import com.nith.kkbank.dto.UserRequest;

public interface UserService {

    BankResponse createAccount(UserRequest userRequest);
}
