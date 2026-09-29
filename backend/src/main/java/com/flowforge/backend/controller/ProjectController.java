package com.flowforge.backend.controller;

import com.flowforge.backend.dto.ProjectDtos.ProjectRequest;
import com.flowforge.backend.dto.ProjectDtos.ProjectResponse;
import com.flowforge.backend.dto.TaskDtos.TaskRequest;
import com.flowforge.backend.dto.TaskDtos.TaskResponse;
import com.flowforge.backend.model.ProjectStatus;
import com.flowforge.backend.model.TaskPriority;
import com.flowforge.backend.model.TaskStatus;
import com.flowforge.backend.security.AuthUser;
import com.flowforge.backend.service.ProjectService;
import com.flowforge.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    /** GET /api/projects?status=ACTIVE&q=website */
    @GetMapping
    public List<ProjectResponse> list(@AuthenticationPrincipal AuthUser user,
                                      @RequestParam(required = false) ProjectStatus status,
                                      @RequestParam(required = false) String q) {
        return projectService.list(user.id(), status, q);
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return projectService.get(id, user.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request,
                                  @AuthenticationPrincipal AuthUser user) {
        return projectService.create(request, user.id());
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request,
                                  @AuthenticationPrincipal AuthUser user) {
        return projectService.update(id, request, user.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        projectService.delete(id, user.id());
    }

    /** GET /api/projects/{id}/tasks?status=TODO&priority=HIGH&sort=dueDate|priority */
    @GetMapping("/{projectId}/tasks")
    public List<TaskResponse> tasks(@PathVariable Long projectId,
                                    @AuthenticationPrincipal AuthUser user,
                                    @RequestParam(required = false) TaskStatus status,
                                    @RequestParam(required = false) TaskPriority priority,
                                    @RequestParam(required = false) String sort) {
        return taskService.listForProject(projectId, user.id(), status, priority, sort);
    }

    @PostMapping("/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@PathVariable Long projectId, @Valid @RequestBody TaskRequest request,
                                   @AuthenticationPrincipal AuthUser user) {
        return taskService.create(projectId, request, user.id());
    }
}
