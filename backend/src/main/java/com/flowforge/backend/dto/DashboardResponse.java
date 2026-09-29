package com.flowforge.backend.dto;

import com.flowforge.backend.dto.ProjectDtos.ProjectResponse;
import com.flowforge.backend.dto.TaskDtos.TaskResponse;
import com.flowforge.backend.model.ProjectStatus;
import com.flowforge.backend.model.TaskStatus;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalProjects,
        Map<ProjectStatus, Long> projectsByStatus,
        long totalTasks,
        Map<TaskStatus, Long> tasksByStatus,
        /* Open tasks that are overdue or due within the next 7 days. */
        List<TaskResponse> dueSoon,
        List<ProjectResponse> recentProjects
) {
}
