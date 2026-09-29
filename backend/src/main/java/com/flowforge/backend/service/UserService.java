package com.flowforge.backend.service;

import com.flowforge.backend.dto.UserResponse;
import com.flowforge.backend.exception.Errors.UserNotFoundException;
import com.flowforge.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return users.findById(userId).map(UserResponse::from).orElseThrow(UserNotFoundException::new);
    }
}
