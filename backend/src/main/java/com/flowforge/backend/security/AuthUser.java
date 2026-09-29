package com.flowforge.backend.security;

/**
 * The authenticated principal, built from a verified JWT.
 * Controllers receive it with {@code @AuthenticationPrincipal AuthUser user}.
 */
public record AuthUser(Long id, String email, String name) {
}
