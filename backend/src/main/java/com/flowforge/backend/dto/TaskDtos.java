package com.flowforge.backend.dto;

import com.flowforge.backend.model.Task;
import com.flowforge.backend.model.TaskPriority;
import com.flowforge.backend.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TaskDtos {

    private TaskDtos() {
    }

    /**
     * Body for POST /api/projects/{id}/tasks and PUT /api/tasks/{id}.
     * Missing status defaults to TODO and missing priority to MEDIUM.
     */
    public record TaskRequest(
            @NotBlank(message = "Task title cannot be empty")
            @Size(max = 200, message = "Task title must be at most 200 characters")
            String title,

            @Size(max = 4000, message = "Description must be at most 4000 characters")
            String description,

            TaskStatus status,
            TaskPriority priority,

            /* ISO date, e.g. 2026-10-15 */
            LocalDate dueDate,

            Long assignedUserId
    ) {
    }

    public record TaskStatusRequest(@NotNull(message = "Status is required") TaskStatus status) {
    }

    public record TaskResponse(
            Long id,
            Long projectId,
            String projectName,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            UserSummary assignedUser,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static TaskResponse from(Task task) {
            return new TaskResponse(
                    task.getId(),
                    task.getProject().getId(),
                    task.getProject().getName(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getPriority(),
                    task.getDueDate(),
                    UserSummary.from(task.getAssignedUser()),
                    task.getCreatedAt(),
                    task.getUpdatedAt());
        }
    }
}
