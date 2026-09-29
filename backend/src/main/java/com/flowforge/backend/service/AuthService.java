package com.flowforge.backend.service;

import com.flowforge.backend.dto.AuthDtos.AuthResponse;
import com.flowforge.backend.dto.AuthDtos.LoginRequest;
import com.flowforge.backend.dto.AuthDtos.RegisterRequest;
import com.flowforge.backend.dto.UserResponse;
import com.flowforge.backend.exception.Errors.EmailAlreadyUsedException;
import com.flowforge.backend.exception.Errors.InvalidCredentialsException;
import com.flowforge.backend.model.User;
import com.flowforge.backend.repository.UserRepository;
import com.flowforge.backend.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    /** Compared against when the email is unknown, so both failure paths take similar time. */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("flowforge-timing-equaliser");
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normaliseEmail(req.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException();
        }
        User user = new User(req.name().trim(), email, passwordEncoder.encode(req.password()));
        try {
            user = users.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Two concurrent registrations with the same email.
            throw new EmailAlreadyUsedException();
        }
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(normaliseEmail(req.email())).orElse(null);
        if (user == null) {
            passwordEncoder.matches(req.password(), dummyHash);
            throw new InvalidCredentialsException();
        }
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(jwtService.createToken(user), jwtService.lifetimeSeconds(), UserResponse.from(user));
    }

    private static String normaliseEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
