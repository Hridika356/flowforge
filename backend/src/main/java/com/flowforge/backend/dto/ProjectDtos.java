package com.flowforge.backend.dto;

import com.flowforge.backend.model.Project;
import com.flowforge.backend.model.ProjectStatus;
import com.flowforge.backend.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public final class ProjectDtos {

    private ProjectDtos() {
    }

    /**
     * Body for POST and PUT /api/projects. A missing status defaults to PLANNING on create
     * and leaves the current status unchanged on update.
     */
    public record ProjectRequest(
            @NotBlank(message = "Project name cannot be empty")
            @Size(max = 150, message = "Project name must be at most 150 characters")
            String name,

            @Size(max = 2000, message = "Description must be at most 2000 characters")
            String description,

            ProjectStatus status
    ) {
    }

    public record ProjectResponse(
            Long id,
            String name,
            String description,
            ProjectStatus status,
            UserSummary owner,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Map<TaskStatus, Long> taskCounts,
            long totalTasks,
            /* Percentage of tasks in DONE, 0-100; 0 for a project with no tasks. */
            int progress
    ) {
        public static ProjectResponse from(Project project, Map<TaskStatus, Long> counts) {
            long total = counts.values().stream().mapToLong(Long::longValue).sum();
            long done = counts.getOrDefault(TaskStatus.DONE, 0L);
            int progress = total == 0 ? 0 : (int) Math.round(done * 100.0 / total);
            return new ProjectResponse(
                    project.getId(),
                    project.getName(),
                    project.getDescription(),
                    project.getStatus(),
                    UserSummary.from(project.getOwner()),
                    project.getCreatedAt(),
                    project.getUpdatedAt(),
                    counts,
                    total,
                    progress);
        }
    }
}
