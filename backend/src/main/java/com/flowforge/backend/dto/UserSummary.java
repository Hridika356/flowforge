package com.flowforge.backend.dto;

import com.flowforge.backend.model.User;

/** Minimal user reference embedded in project and task responses (no email). */
public record UserSummary(Long id, String name) {

    public static UserSummary from(User user) {
        return user == null ? null : new UserSummary(user.getId(), user.getName());
    }
}
