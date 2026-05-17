package com.onlinequiz.user.service;

import com.onlinequiz.user.entity.User;

public interface UserQueryService {

    boolean existsByEmail(String email);

    User getByEmail(String email);

    User getById(Long id);
}
