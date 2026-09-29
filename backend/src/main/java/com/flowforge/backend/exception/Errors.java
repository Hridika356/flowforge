package com.flowforge.backend.exception;

import org.springframework.http.HttpStatus;

/** Concrete API exceptions. */
public final class Errors {

    private Errors() {
    }

    /**
     * Also used when a resource exists but belongs to someone else, so that IDs of
     * other users' projects and tasks can't be discovered by probing.
     */
    public static class ResourceNotFoundException extends ApiException {
        public ResourceNotFoundException(String message) {
            super(HttpStatus.NOT_FOUND, message);
        }
    }

    public static class ProjectNotFoundException extends ResourceNotFoundException {
        public ProjectNotFoundException() {
            super("Project not found");
        }
    }

    public static class TaskNotFoundException extends ResourceNotFoundException {
        public TaskNotFoundException() {
            super("Task not found");
        }
    }

    public static class UserNotFoundException extends ResourceNotFoundException {
        public UserNotFoundException() {
            super("User not found");
        }
    }

    public static class InvalidCredentialsException extends ApiException {
        public InvalidCredentialsException() {
            super(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    public static class UnauthorizedException extends ApiException {
        public UnauthorizedException(String message) {
            super(HttpStatus.FORBIDDEN, message);
        }
    }

    public static class EmailAlreadyUsedException extends ApiException {
        public EmailAlreadyUsedException() {
            super(HttpStatus.CONFLICT, "An account with this email already exists");
        }
    }

    public static class BadRequestException extends ApiException {
        public BadRequestException(String message) {
            super(HttpStatus.BAD_REQUEST, message);
        }
    }
}
