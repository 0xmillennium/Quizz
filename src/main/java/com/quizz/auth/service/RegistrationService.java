package com.quizz.auth.service;

import com.quizz.auth.dto.RegisterRequest;

public interface RegistrationService {

    void register(RegisterRequest request);
}
