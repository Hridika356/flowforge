package com.flowforge.backend.model;

/**
 * Fixed MVP workflow. Declaration order is the board column order.
 * Custom workflows (spec section 9) would replace this with a workflow_statuses table.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    REVIEW,
    DONE
}
