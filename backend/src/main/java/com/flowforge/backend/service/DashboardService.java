package com.flowforge.backend.service;

import com.flowforge.backend.dto.DashboardResponse;
import com.flowforge.backend.dto.TaskDtos.TaskResponse;
import com.flowforge.backend.model.Project;
import com.flowforge.backend.model.ProjectStatus;
import com.flowforge.backend.model.TaskStatus;
import com.flowforge.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    static final int DUE_SOON_DAYS = 7;
    static final int RECENT_PROJECTS = 5;

    private final ProjectAccessService access;
    private final ProjectService projectService;
    private final TaskCountService taskCounts;
    private final TaskRepository tasks;

    public DashboardService(ProjectAccessService access, ProjectService projectService,
                            TaskCountService taskCounts, TaskRepository tasks) {
        this.access = access;
        this.projectService = projectService;
        this.taskCounts = taskCounts;
        this.tasks = tasks;
    }

    @Transactional(readOnly = true)
    public DashboardResponse forUser(Long userId) {
        List<Project> projects = access.visibleProjects(userId);

        Map<ProjectStatus, Long> byStatus = new EnumMap<>(ProjectStatus.class);
        for (ProjectStatus s : ProjectStatus.values()) {
            byStatus.put(s, 0L);
        }
        projects.forEach(p -> byStatus.merge(p.getStatus(), 1L, Long::sum));

        Map<TaskStatus, Long> taskStatus = taskCounts.countsForOwner(userId);
        long totalTasks = taskStatus.values().stream().mapToLong(Long::longValue).sum();

        List<TaskResponse> dueSoon = tasks
                .findOpenDueBy(userId, LocalDate.now().plusDays(DUE_SOON_DAYS), TaskStatus.DONE)
                .stream().limit(10).map(TaskResponse::from).toList();

        return new DashboardResponse(
                projects.size(),
                byStatus,
                totalTasks,
                taskStatus,
                dueSoon,
                projectService.toResponses(projects.stream().limit(RECENT_PROJECTS).toList()));
    }
}
