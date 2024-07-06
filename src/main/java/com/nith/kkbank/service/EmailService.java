package com.nith.kkbank.service;

import com.nith.kkbank.dto.EmailDetails;

public interface EmailService {

    void sendEmailAlert(EmailDetails emailDetails);
}
