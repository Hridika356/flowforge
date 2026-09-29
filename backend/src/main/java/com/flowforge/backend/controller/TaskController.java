package com.flowforge.backend.controller;

import com.flowforge.backend.dto.TaskDtos.TaskRequest;
import com.flowforge.backend.dto.TaskDtos.TaskResponse;
import com.flowforge.backend.dto.TaskDtos.TaskStatusRequest;
import com.flowforge.backend.security.AuthUser;
import com.flowforge.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Operations on an individual task. Listing and creating live under /api/projects/{id}/tasks. */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{taskId}")
    public TaskResponse get(@PathVariable Long taskId, @AuthenticationPrincipal AuthUser user) {
        return taskService.get(taskId, user.id());
    }

    @PutMapping("/{taskId}")
    public TaskResponse update(@PathVariable Long taskId, @Valid @RequestBody TaskRequest request,
                               @AuthenticationPrincipal AuthUser user) {
        return taskService.update(taskId, request, user.id());
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateStatus(@PathVariable Long taskId, @Valid @RequestBody TaskStatusRequest request,
                                     @AuthenticationPrincipal AuthUser user) {
        return taskService.updateStatus(taskId, request.status(), user.id());
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long taskId, @AuthenticationPrincipal AuthUser user) {
        taskService.delete(taskId, user.id());
    }
}
