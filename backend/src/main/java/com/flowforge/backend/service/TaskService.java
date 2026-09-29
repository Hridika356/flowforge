package com.flowforge.backend.service;

import com.flowforge.backend.dto.TaskDtos.TaskRequest;
import com.flowforge.backend.dto.TaskDtos.TaskResponse;
import com.flowforge.backend.exception.Errors.BadRequestException;
import com.flowforge.backend.model.Project;
import com.flowforge.backend.model.Task;
import com.flowforge.backend.model.TaskPriority;
import com.flowforge.backend.model.TaskStatus;
import com.flowforge.backend.model.User;
import com.flowforge.backend.repository.TaskRepository;
import com.flowforge.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository tasks;
    private final UserRepository users;
    private final ProjectAccessService access;

    public TaskService(TaskRepository tasks, UserRepository users, ProjectAccessService access) {
        this.tasks = tasks;
        this.users = users;
        this.access = access;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listForProject(Long projectId, Long userId,
                                             TaskStatus status, TaskPriority priority, String sort) {
        access.requireReadable(projectId, userId);
        var stream = tasks.findByProjectId(projectId).stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> priority == null || t.getPriority() == priority);
        if ("dueDate".equals(sort)) {
            stream = stream.sorted(Comparator.comparing(Task::getDueDate,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("priority".equals(sort)) {
            stream = stream.sorted(Comparator.comparing(Task::getPriority).reversed());
        }
        return stream.map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long taskId, Long userId) {
        return TaskResponse.from(access.requireEditableTask(taskId, userId));
    }

    @Transactional
    public TaskResponse create(Long projectId, TaskRequest req, Long userId) {
        Project project = access.requireEditable(projectId, userId);
        Task task = new Task(project);
        apply(task, req, project);
        return TaskResponse.from(tasks.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse update(Long taskId, TaskRequest req, Long userId) {
        Task task = access.requireEditableTask(taskId, userId);
        apply(task, req, task.getProject());
        return TaskResponse.from(tasks.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse updateStatus(Long taskId, TaskStatus status, Long userId) {
        Task task = access.requireEditableTask(taskId, userId);
        task.setStatus(status);
        return TaskResponse.from(tasks.saveAndFlush(task));
    }

    @Transactional
    public void delete(Long taskId, Long userId) {
        tasks.delete(access.requireEditableTask(taskId, userId));
    }

    private void apply(Task task, TaskRequest req, Project project) {
        task.setTitle(req.title().trim());
        task.setDescription(ProjectService.blankToNull(req.description()));
        task.setStatus(req.status() == null ? TaskStatus.TODO : req.status());
        task.setPriority(req.priority() == null ? TaskPriority.MEDIUM : req.priority());
        task.setDueDate(req.dueDate());
        task.setAssignedUser(resolveAssignee(req.assignedUserId(), project));
    }

    private User resolveAssignee(Long assigneeId, Project project) {
        if (assigneeId == null) {
            return null;
        }
        User assignee = users.findById(assigneeId).orElse(null);
        if (assignee == null || !access.canBeAssigned(assignee, project)) {
            throw new BadRequestException("That user can't be assigned to tasks in this project");
        }
        return assignee;
    }
}
