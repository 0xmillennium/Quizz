package com.onlinequiz.user.service;

import com.onlinequiz.user.dto.CreateUserCommand;
import com.onlinequiz.user.entity.User;

public interface UserAccountService {

    User createUser(CreateUserCommand command);
}
