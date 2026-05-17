package com.quizz.user.service;

import com.quizz.user.entity.User;

public interface UserAccountService {

    User createUser(CreateUserCommand command);
}
