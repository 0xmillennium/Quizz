package com.quizz.user.service;

import com.quizz.user.entity.User;

public interface UserQueryService {

    boolean existsByEmail(String email);

    User getByEmail(String email);

    User getById(Long id);
}
