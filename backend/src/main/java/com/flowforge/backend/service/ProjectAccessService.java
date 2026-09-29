package com.flowforge.backend.service;

import com.flowforge.backend.exception.Errors.ProjectNotFoundException;
import com.flowforge.backend.exception.Errors.TaskNotFoundException;
import com.flowforge.backend.model.Project;
import com.flowforge.backend.model.Task;
import com.flowforge.backend.model.User;
import com.flowforge.backend.repository.ProjectRepository;
import com.flowforge.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * The single place that decides who may see or change a project and its tasks.
 *
 * <p>In the MVP only the project owner has access. When ProjectMember and roles are added,
 * change the methods here (for example, members may read, ADMIN/OWNER may edit) and every
 * controller and service picks the new rules up automatically.
 *
 * <p>Projects and tasks the caller can't access are reported as "not found" rather than
 * "forbidden", so their IDs can't be discovered by probing.
 */
@Service
public class ProjectAccessService {

    private final ProjectRepository projects;
    private final TaskRepository tasks;

    public ProjectAccessService(ProjectRepository projects, TaskRepository tasks) {
        this.projects = projects;
        this.tasks = tasks;
    }

    /** Projects the user can see, most recently updated first. */
    public List<Project> visibleProjects(Long userId) {
        return projects.findByOwnerIdOrderByUpdatedAtDesc(userId);
    }

    public Project requireReadable(Long projectId, Long userId) {
        return projects.findByIdAndOwnerId(projectId, userId).orElseThrow(ProjectNotFoundException::new);
    }

    public Project requireEditable(Long projectId, Long userId) {
        return projects.findByIdAndOwnerId(projectId, userId).orElseThrow(ProjectNotFoundException::new);
    }

    public Project requireDeletable(Long projectId, Long userId) {
        return projects.findByIdAndOwnerId(projectId, userId).orElseThrow(ProjectNotFoundException::new);
    }

    public Task requireEditableTask(Long taskId, Long userId) {
        return tasks.findByIdAndProjectOwnerId(taskId, userId).orElseThrow(TaskNotFoundException::new);
    }

    /** Whether a task in this project may be assigned to the given user. */
    public boolean canBeAssigned(User assignee, Project project) {
        return Objects.equals(assignee.getId(), project.getOwner().getId());
    }
}
