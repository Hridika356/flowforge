package com.flowforge.backend.service;

import com.flowforge.backend.dto.ProjectDtos.ProjectRequest;
import com.flowforge.backend.dto.ProjectDtos.ProjectResponse;
import com.flowforge.backend.exception.Errors.UserNotFoundException;
import com.flowforge.backend.model.Project;
import com.flowforge.backend.model.ProjectStatus;
import com.flowforge.backend.model.TaskStatus;
import com.flowforge.backend.model.User;
import com.flowforge.backend.repository.ProjectRepository;
import com.flowforge.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ProjectService {

    private final ProjectRepository projects;
    private final UserRepository users;
    private final ProjectAccessService access;
    private final TaskCountService taskCounts;

    public ProjectService(ProjectRepository projects, UserRepository users,
                          ProjectAccessService access, TaskCountService taskCounts) {
        this.projects = projects;
        this.users = users;
        this.access = access;
        this.taskCounts = taskCounts;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(Long userId, ProjectStatus status, String query) {
        List<Project> result;
        if (query != null && !query.isBlank()) {
            result = projects.searchOwned(userId, query.trim());
        } else {
            result = access.visibleProjects(userId);
        }
        if (status != null) {
            result = result.stream().filter(p -> p.getStatus() == status).toList();
        }
        return toResponses(result);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long projectId, Long userId) {
        return toResponse(access.requireReadable(projectId, userId));
    }

    @Transactional
    public ProjectResponse create(ProjectRequest req, Long userId) {
        User owner = users.findById(userId).orElseThrow(UserNotFoundException::new);
        Project project = new Project(
                req.name().trim(),
                blankToNull(req.description()),
                req.status() == null ? ProjectStatus.PLANNING : req.status(),
                owner);
        return toResponse(projects.saveAndFlush(project));
    }

    @Transactional
    public ProjectResponse update(Long projectId, ProjectRequest req, Long userId) {
        Project project = access.requireEditable(projectId, userId);
        project.setName(req.name().trim());
        project.setDescription(blankToNull(req.description()));
        if (req.status() != null) {
            project.setStatus(req.status());
        }
        return toResponse(projects.saveAndFlush(project));
    }

    @Transactional
    public void delete(Long projectId, Long userId) {
        Project project = access.requireDeletable(projectId, userId);
        projects.delete(project); // tasks are removed by cascade
    }

    List<ProjectResponse> toResponses(List<Project> list) {
        Map<Long, Map<TaskStatus, Long>> counts =
                taskCounts.countsByProject(list.stream().map(Project::getId).toList());
        return list.stream().map(p -> ProjectResponse.from(p, counts.get(p.getId()))).toList();
    }

    private ProjectResponse toResponse(Project project) {
        return toResponses(List.of(project)).get(0);
    }

    static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
