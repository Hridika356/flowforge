package com.flowforge.backend.service;

import com.flowforge.backend.model.TaskStatus;
import com.flowforge.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Per-status task counts, fetched in one grouped query instead of loading every task. */
@Service
public class TaskCountService {

    private final TaskRepository tasks;

    public TaskCountService(TaskRepository tasks) {
        this.tasks = tasks;
    }

    public Map<Long, Map<TaskStatus, Long>> countsByProject(Collection<Long> projectIds) {
        Map<Long, Map<TaskStatus, Long>> result = new HashMap<>();
        for (Long id : projectIds) {
            result.put(id, emptyCounts());
        }
        if (projectIds.isEmpty()) {
            return result;
        }
        for (Object[] row : tasks.countByProjectAndStatus(projectIds)) {
            result.get((Long) row[0]).put((TaskStatus) row[1], (Long) row[2]);
        }
        return result;
    }

    public Map<TaskStatus, Long> countsForOwner(Long ownerId) {
        Map<TaskStatus, Long> counts = emptyCounts();
        for (Object[] row : tasks.countByStatusForOwner(ownerId)) {
            counts.put((TaskStatus) row[0], (Long) row[1]);
        }
        return counts;
    }

    public static Map<TaskStatus, Long> emptyCounts() {
        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        for (TaskStatus s : TaskStatus.values()) {
            counts.put(s, 0L);
        }
        return counts;
    }
}
