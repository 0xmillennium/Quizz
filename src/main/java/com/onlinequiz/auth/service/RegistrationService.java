package com.onlinequiz.auth.service;

import com.onlinequiz.auth.dto.RegisterRequest;

public interface RegistrationService {

    void register(RegisterRequest request);
}
